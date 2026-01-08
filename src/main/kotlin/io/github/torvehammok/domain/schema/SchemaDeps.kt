@file:Suppress("UnstableApiUsage")

package io.github.torvehammok.domain.schema

import com.google.common.graph.ElementOrder.sorted
import com.google.common.graph.Graph
import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema
import io.github.torvehammok.domain.sandbox.SandboxProps
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.PathMatcher

class SchemaDeps(private val sandboxProps: SandboxProps) {

    fun resolveSchemaDeps(schemasDir: Path, matcher: PathMatcher = PathMatcher { true }): SchemasGraph {
        val graph = traverseDirsAndBuildSchemasGraph(schemasDir, matcher)

        return GuavaSchemasGraph(graph)
    }

    private fun traverseDirsAndBuildSchemasGraph(protoPath: Path, modulePathMatcher: PathMatcher): Graph<SchemaDef> {
        val graph = createDependenciesGraph()
        // walk through directory and print all .proto files
        Files.walk(protoPath).use { paths ->
            for (path in paths) {
                if (!Files.isRegularFile(path) || !path.toString().endsWith(".proto")) {
                    continue
                }
                val relativeProtoFile = protoPath.relativize(path)

                if (!modulePathMatcher.matches(relativeProtoFile)) {
                    continue
                }

                val protobufSchema = ProtobufSchema(Files.readString(path))
                val protoFile = protobufSchema.rawSchema()

                val allImports = protoFile.imports
                    .plus(protoFile.publicImports)
                    .plus(protoFile.weakImports)
                    .filter { !it.startsWith("google/protobuf/") }

                val packageName = protoFile.packageName ?: "default"

                val schemaDef = SchemaDef(
                    subject = toSubjectName(relativeProtoFile.toString(), sandboxProps),
                    filename = relativeProtoFile.toString(),
                    packageName = packageName
                )
                graph.addNode(schemaDef)

                for (import in allImports) {
                    val importedPath = protoPath.resolve(import)

                    if (!Files.exists(importedPath)) {
                        throw IllegalStateException("Imported proto file not found: $import in $relativeProtoFile")
                    }

                    val rawProto = Files.readString(importedPath)
                    val importedSchema = ProtobufSchema(rawProto)

                    val dependentSchema = SchemaDef(
                        subject = toSubjectName(import, sandboxProps),
                        filename = import,
                        packageName = importedSchema.rawSchema().packageName ?: "default"
                    )

                    graph.putEdge(dependentSchema, schemaDef)
                }
            }
        }

        return graph
    }

    fun resolveSchemaDeps(schemas: List<RegistrySchema>): GuavaSchemasGraph {
        val graph = createDependenciesGraph()

        for (schema in schemas) {
            val def = SchemaDef(
                subject = schema.subject,
                filename = schema.subject,
                packageName = "default"
            )

            val refs = schema.refs
                .map { ref ->
                    val refSchema = schemas.find { it.subject == ref.subject }
                        ?: throw IllegalStateException("Referenced schema not found: ${ref.subject} in ${schema.subject}")

                    SchemaDef(
                        subject = refSchema.subject,
                        filename = refSchema.subject,
                        packageName = "default"
                    )
                }
            graph.addNode(def)
            for (ref in refs) {
                graph.putEdge(ref, def)
            }
        }

        return GuavaSchemasGraph(graph)
    }

}

private fun createDependenciesGraph(): MutableGraph<SchemaDef> {
    return GraphBuilder.directed()
        .nodeOrder(
            sorted(Comparator.comparing<SchemaDef, String> { it.subject })
        )
        .build()
}
