package io.github.torvehammok.domain.schema

import de.danielbechler.diff.ObjectDiffer
import de.danielbechler.diff.ObjectDifferBuilder
import de.danielbechler.diff.identity.EqualsIdentityStrategy
import de.danielbechler.diff.node.DiffNode.State.*
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema
import io.github.torvehammok.cli.SchemasSpecProps
import io.github.torvehammok.domain.sandbox.SandboxProps
import org.slf4j.LoggerFactory
import java.nio.file.*

private val log = LoggerFactory.getLogger(SchemaService::class.java)

class SchemaService(
    private val registryClient: RegistryClient,
    private val schemasSpecProps: SchemasSpecProps,
    private val schemaDeps: SchemaDeps
) {

    fun listSchemas(): List<String> {
        val listSchemas = registryClient.listSchemas()

        return listSchemas.map { it.subject }
    }

    fun destroySchemas() {
        log.info("Destroying all schemas...")

        val allSchemas = registryClient.listSchemas()
        val depsGraph = schemaDeps.resolveSchemaDeps(allSchemas)

        depsGraph.dependencyGraphItems().forEach {
            log.info("Deleting schema: ${it.subject}")
            // Deletion not supported in this example
            registryClient.deleteSchema(it.subject)
        }
    }

    fun planSchemaUpdates(inclusionGlobs: List<String>? = null): SchemaUpdatePlan {
        log.info("Planning schema updates...")
        val dir = Paths.get(schemasSpecProps.dir)

        val depsGraph = schemaDeps.resolveSchemaDeps(
            schemasDir = dir,
            matcher = inclusionGlobMatcher(inclusionGlobs)
        )

        val currentSchemas = registryClient.listSchemas()
        val expectedSchemas = toExpectedSchemas(depsGraph, dir, currentSchemas)

        val plan = calcSchemaUpdatesPlan(expectedSchemas, currentSchemas, depsGraph)

        log.info("Schema updates plan:\n{}", SchemaUpdatePlanPrinter().print(plan))
        return plan
    }

    fun listSchemasNamespaces(namespace: String? = null): List<NamespaceSchemas> {
        val depsGraph = schemaDeps.resolveSchemaDeps(Paths.get(schemasSpecProps.dir))
        return depsGraph.listNamespaces(namespace = namespace)
    }

    fun applySchemaUpdates(inclusionGlobs: List<String>? = null): SchemaUpdatePlan {
        log.info("Planning schema updates...")
        val dir = Paths.get(schemasSpecProps.dir)
        val depsGraph = schemaDeps.resolveSchemaDeps(
            schemasDir = dir,
            matcher = inclusionGlobMatcher(inclusionGlobs)
        )
        val currentSchemas = registryClient.listSchemas()

        val expectedSchemas = toExpectedSchemas(depsGraph, dir, currentSchemas)

        val plan = calcSchemaUpdatesPlan(expectedSchemas, currentSchemas, depsGraph)

        log.info("Schema updates plan:\n{}", SchemaUpdatePlanPrinter().print(plan))

        log.info("Applying schema updates...")
        applySchemaUpdatesPlan(plan, currentSchemas)
        return plan
    }

    private fun inclusionGlobMatcher(inclusionPatterns: List<String>?): PathMatcher {
        val patterns = inclusionPatterns ?: schemasSpecProps.includeOnly.map { it.glob }
        return toInclusionPathMatcher(patterns)
    }

    private fun calcSchemaUpdatesPlan(
        expectedSchemas: List<RegistrySchema>,
        currentSchemas: List<RegistrySchema>,
        depsGraph: SchemasGraph
    ): SchemaUpdatePlan {
        val differ = schemasDiffer()
        val ops = mutableListOf<SchemaOp>()

        for (expectedSchema in expectedSchemas) {
            val currentSchema = currentSchemas.find { it.subject == expectedSchema.subject }
            val diff = differ.compare(expectedSchema, currentSchema)

            when (diff.state) {
                ADDED -> ops.add(SchemaOp(ADDED, expectedSchema))

                CHANGED -> {
                    val refsChanged = diff.getChild("refs").state == CHANGED
                    val definitionChanged = diff.getChild("definition").state == CHANGED

                    ops.add(
                        SchemaOp(
                            state = CHANGED,
                            schema = expectedSchema,
                            definitionChanged = definitionChanged,
                            refsChanged = refsChanged
                        )
                    )
                }

                UNTOUCHED -> ops.add(SchemaOp(UNTOUCHED, expectedSchema))

                else -> throw IllegalStateException("Unexpected diff state: ${diff.state} for schema ${expectedSchema.subject}")
            }
        }

        return SchemaUpdatePlan(ops, depsGraph)
    }

    private fun toExpectedSchemas(
        depsGraph: SchemasGraph,
        dir: Path,
        currentSchemas: List<RegistrySchema>
    ): List<RegistrySchema> {
        val schemaDefinitions = depsGraph.allSchemas()

        val expectedSchemas = schemaDefinitions.map {
            val protobufSchema = ProtobufSchema(Files.readString(dir.resolve(it.filename)))
            val deps = depsGraph.findDepsForSubject(it.subject)

            val refs = deps.map { ref ->
                val refSchema = currentSchemas.find { s -> s.subject == ref.subject }

                RegistrySchemaRef(
                    name = ref.filename,
                    subject = ref.subject,
                    version = refSchema?.version ?: -1,
                )
            }

            RegistrySchema(
                id = -1,
                subject = it.subject,
                version = 0,
                definition = protobufSchema.canonicalString(),
                refs = refs
            )
        }

        return expectedSchemas
    }

    private fun applySchemaUpdatesPlan(plan: SchemaUpdatePlan, currentSchemas: List<RegistrySchema>) {
        fun updateSubjectVersion(subject: String, version: Int) {
            currentSchemas.find { it.subject == subject }?.version = version
        }

        for (schema in plan.changes()) {
            val op = plan.ops.find { it.schema.subject == schema.subject }!!

            if (op.state == UNTOUCHED) {
                log.info("  ~ Updating schema '${schema.subject}' references due to upstream")

                val nextVersion = registryClient.updateSchema(
                    subject = schema.subject,
                    schemaDefinition = op.schema.definition,
                    refs = toLatestSchemaRefs(op.schema.refs, currentSchemas)
                )
                updateSubjectVersion(schema.subject, nextVersion)

                continue
            }

            when (op.state) {

                ADDED -> {
                    log.info("+ Creating schema '${op.schema.subject}")
                    val nextVersion = registryClient.updateSchema(
                        subject = op.schema.subject,
                        schemaDefinition = op.schema.definition,
                        refs = toLatestSchemaRefs(op.schema.refs, currentSchemas)
                    )
                    updateSubjectVersion(op.schema.subject, nextVersion)
                }

                CHANGED -> {
                    val line = listOf(
                        if (op.definitionChanged) "definition" else "",
                        if (op.refsChanged) "references" else ""
                    ).filter { it.isNotEmpty() }.joinToString(" & ")

                    log.info("~ Updating schema '${op.schema.subject}' $line")

                    val currentDef = currentSchemas.find { it.subject == op.schema.subject }!!
                    val nextVersion = registryClient.updateSchema(
                        subject = op.schema.subject,
                        schemaDefinition = op.schema.definition,
                        refs = toLatestSchemaRefs(op.schema.refs, currentSchemas),
                        minVersion = currentDef.version
                    )
                    updateSubjectVersion(op.schema.subject, nextVersion)
                }

                else -> {
                    // noop
                }
            }
        }
    }

}

private fun toLatestSchemaRefs(
    refs: List<RegistrySchemaRef>,
    currentSchemas: List<RegistrySchema>
): List<RegistrySchemaRef> {
    return refs.map { ref ->
        val refSchema = currentSchemas.find { s -> s.subject == ref.subject }

        RegistrySchemaRef(
            name = ref.name,
            subject = ref.subject,
            version = refSchema?.version ?: -1,
        )
    }
}

fun toSubjectName(fileName: String, sandboxProps: SandboxProps): String {
    val schemaName = if (fileName.endsWith("-value.proto") || fileName.endsWith("-key.proto")) {
        Paths.get(fileName).fileName.toString().removeSuffix(".proto")
    }
    else if (fileName.endsWith("-value.avsc") || fileName.endsWith("-key.avsc")) {
        Paths.get(fileName).fileName.toString().removeSuffix(".avsc")
    }
    else {
        fileName
    }

    return if (sandboxProps.enabled) {
        sandboxProps.prefix + schemaName
    } else {
        schemaName
    }
}

private fun schemasDiffer(): ObjectDiffer {
    return ObjectDifferBuilder.startBuilding()
        .filtering()
        .returnNodesWithState(UNTOUCHED, true)
        .and()
        .inclusion()
        .exclude()
        .propertyNameOfType(RegistrySchema::class.java, "id")
        .propertyNameOfType(RegistrySchema::class.java, "version")
        .and()
        .identity()
        .setDefaultCollectionItemIdentityStrategy { a, b ->
            if (a is RegistrySchemaRef && b is RegistrySchemaRef) {
                a.subject == b.subject
            } else {
                EqualsIdentityStrategy.getInstance().equals(a, b)
            }
        }
        .and()
        .build()
}

fun toInclusionPathMatcher(inclusionPatterns: List<String>): PathMatcher {
    if (inclusionPatterns.isEmpty()) {
        return PathMatcher { true }
    }

    val modulePathMatcher = inclusionPatterns
        .map { FileSystems.getDefault().getPathMatcher("glob:${it}") }
        .reduce { matcher1, matcher2 ->
            PathMatcher { matcher1.matches(it) || matcher2.matches(it) }
        }

    return modulePathMatcher
}
