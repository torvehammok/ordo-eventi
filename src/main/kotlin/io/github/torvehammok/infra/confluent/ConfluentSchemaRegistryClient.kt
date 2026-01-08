package io.github.torvehammok.infra.confluent

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaReference
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema
import io.github.torvehammok.cli.SchemasSpecProps
import io.github.torvehammok.domain.sandbox.SandboxProps
import io.github.torvehammok.domain.schema.RegistryClient
import io.github.torvehammok.domain.schema.RegistrySchema
import io.github.torvehammok.domain.schema.RegistrySchemaRef
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger(ConfluentSchemaRegistryClient::class.java)

class ConfluentSchemaRegistryClient(
    private val schemasSpecProps: SchemasSpecProps,
    private val sandboxProps: SandboxProps,
    private val client: SchemaRegistryClient,
) : RegistryClient {

    override fun updateSchema(
        subject: String,
        schemaDefinition: String,
        refs: List<RegistrySchemaRef>,
        minVersion: Int
    ): Int {
        val schemaRefs = refs.map { ref -> SchemaReference(ref.name, ref.subject, ref.version) }
        val schema = ProtobufSchema(schemaDefinition, schemaRefs, emptyMap(), null, subject)

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

    override fun deleteAllSchemas() {
        for (i in 1..10) {
            val allSubjects = client.getAllSubjects(false)
            if (allSubjects.isEmpty()) {
                log.info("All schema subjects deleted.")
                return
            }

            for (subject in allSubjects) {
                log.info("Deleting schema subject: $subject")
                try {
                    client.deleteSubject(subject, false)
                } catch (e: Exception) {
                    log.debug("Failed to delete schema subject: $subject. {}", e.message)
                }
            }
        }

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
                file = null,
                refs = latestSchemaMetadata.references.map { ref ->
                    RegistrySchemaRef(
                        name = ref.name,
                        subject = ref.subject,
                        file = null,
                        version = ref.version
                    )
                }
            )

            schemas.add(registrySchema)
        }

        return schemas
    }
}