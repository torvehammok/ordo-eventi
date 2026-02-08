package io.github.torvehammok.domain

import io.confluent.kafka.schemaregistry.avro.AvroSchemaProvider
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchemaProvider
import io.github.cdimascio.dotenv.dotenv
import io.github.torvehammok.OrdoEventiTest
import io.github.torvehammok.cli.SchemaRegistryProps
import io.github.torvehammok.domain.schema.RegistryClient
import io.github.torvehammok.domain.schema.RegistrySchemaRef
import io.github.torvehammok.domain.schema.SchemaService
import io.github.torvehammok.domain.schema.SchemaUpdatePlanPrinter
import io.github.torvehammok.domain.schema.NamespaceSchemas
import io.github.torvehammok.infra.config.YamlFileConfigmap
import io.github.torvehammok.infra.ctx.DefaultCtx
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled
class DestroyAllSchemasTest : OrdoEventiTest() {

    companion object {

        private lateinit var schemaRegistryProps: SchemaRegistryProps

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            dotenv {
                systemProperties = true
                ignoreIfMissing = true
            }

            val systemResource = ClassLoader.getSystemResource("configmap.yaml")

            val configmap = YamlFileConfigmap(systemResource.openStream())

            val ctx = DefaultCtx(configmap)
            schemaRegistryProps = ctx.get(SchemaRegistryProps::class.java)
        }
    }


    @Test
    fun name() {
        val registryClient = CachedSchemaRegistryClient(
            schemaRegistryProps.baseUrl,
            256,
            listOf(ProtobufSchemaProvider(), AvroSchemaProvider()),
            schemaRegistryProps.config
        )


        val allSubjects = registryClient.getAllSubjects(true)

        for (subject in allSubjects) {
            val versions = registryClient.getAllVersions(subject, true)

            try {
                for (version in versions) {
                    println("Deleting subject $subject version $version")
                    registryClient.deleteSchemaVersion(subject, version.toString(), true)
                }

            } catch (ex: Exception) {
                println("Error deleting subject $subject version $versions: ${ex.message}")
            }
        }

    }
}