package io.github.torvehammok.infra.confluent

import io.confluent.kafka.schemaregistry.ParsedSchema
import io.confluent.kafka.schemaregistry.avro.AvroSchema
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaReference
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema
import io.github.torvehammok.cli.SchemasSpecFormat.AVRO
import io.github.torvehammok.cli.SchemasSpecFormat.PROTOBUF
import io.github.torvehammok.cli.SchemasSpecProps
import io.github.torvehammok.domain.sandbox.SandboxProps
import io.github.torvehammok.domain.schema.RegistryClient
import io.github.torvehammok.domain.schema.RegistrySchema
import io.github.torvehammok.domain.schema.RegistrySchemaRef
import io.github.torvehammok.domain.schema.SchemaDef
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

private val log = LoggerFactory.getLogger(ConfluentSchemaRegistryClient::class.java)

class ConfluentSchemaRegistryClient(
    private val schemasSpecProps: SchemasSpecProps,
    private val sandboxProps: SandboxProps,
    private val client: SchemaRegistryClient,
) : RegistryClient {

    override fun updateSchema(
        subject: String,
        schemaDefinition: String,
        directReferences: List<RegistrySchemaRef>,
        minVersion: Int,
        allReferences: List<SchemaDef>,
    ): Int {
        val schemaRefs = directReferences
            .map { ref -> SchemaReference(ref.name, ref.subject, ref.version) }

        val schema = toParsedSchema(
            schemaDefinition = schemaDefinition,
            directReferences = schemaRefs,
            allReferences = allReferences
        )

        val response = client.registerWithResponse(subject, schema, false, false)

        if (response.version <= minVersion) {
            // Schema registry has reused previous version, the configuration should be updated to compare
            // schema definitino with the version returned by the registry
            log.warn(
                """
                Schema registry returned version ${response.version} for subject '$subject',
                which is less than or equal to the minimum expected version $minVersion.
                This happens when the schema definition matches an existing version in the registry.
                To ensure proper versioning, the schema definition will be updated to force a new version registration
                """.trimIndent()
            )
        }

        client.updateCompatibility(subject, schemasSpecProps.defaults.compatibility)
        return response.version
    }

    override fun deleteSchema(subject: String) {
        client.deleteSubject(subject, false)
    }

    override fun normalizeSchemaDef(schema: SchemaDef, refs: List<SchemaDef>): String {
        val file = toSchemaFile(schema)

        val rawSchema = Files.readString(file)

        val parsedSchema = toParsedSchema(
            schemaDefinition = rawSchema,
            directReferences = emptyList(), // Direct references are not needed for normalization
            allReferences = refs
        )

        return parsedSchema.canonicalString()
    }

    override fun listSchemas(): List<RegistrySchema> {
        val allSubjects = if (sandboxProps.enabled) {
            client.getAllSubjectsByPrefix(sandboxProps.prefix)
        } else {
            client.getAllSubjects(false)
        }

        val schemas = mutableListOf<RegistrySchema>()
        for (subject in allSubjects) {
            val latestSchemaMetadata = client.getLatestSchemaMetadata(subject)

            val registrySchema = RegistrySchema(
                subject = latestSchemaMetadata.subject,
                version = latestSchemaMetadata.version,
                definition = latestSchemaMetadata.schema,
                id = latestSchemaMetadata.id,
                refs = latestSchemaMetadata.references.map { ref ->
                    RegistrySchemaRef(
                        name = ref.name,
                        subject = ref.subject,
                        version = ref.version
                    )
                }
            )

            schemas.add(registrySchema)
        }

        return schemas
    }

    private fun toParsedSchema(
        schemaDefinition: String,
        directReferences: List<SchemaReference>,
        allReferences: List<SchemaDef>
    ): ParsedSchema {
        return when (schemasSpecProps.format) {
            AVRO -> {
                val resolvedRefs = allReferences.associate {
                    val file = toSchemaFile(it)
                    val rawSchema = Files.readString(file)
                    it.name to rawSchema
                }

                AvroSchema(schemaDefinition, directReferences, resolvedRefs, null)
            }

            PROTOBUF -> ProtobufSchema(schemaDefinition, directReferences, emptyMap(), null, null)
        }
    }

    private fun toSchemaFile(def: SchemaDef): Path {
        return Paths.get(schemasSpecProps.dir).resolve(def.filename)
    }
}
