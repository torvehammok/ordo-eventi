package io.github.torvehammok.infra.ctx

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchemaProvider
import io.github.torvehammok.Configmap
import io.github.torvehammok.Ctx
import io.github.torvehammok.cli.SchemaRegistryProps
import io.github.torvehammok.cli.SchemasSpecProps
import io.github.torvehammok.cli.TopicsSpecProps
import io.github.torvehammok.domain.sandbox.SandboxProps
import io.github.torvehammok.domain.schema.RegistryClient
import io.github.torvehammok.domain.schema.SchemaDeps
import io.github.torvehammok.domain.schema.SchemaService
import io.github.torvehammok.domain.topic.AdminClientsPool
import io.github.torvehammok.domain.topic.TopicOps
import io.github.torvehammok.domain.topic.TopicService
import io.github.torvehammok.infra.confluent.ConfluentSchemaRegistryClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val log: Logger = LoggerFactory.getLogger(DefaultCtx::class.java)

class DefaultCtx(val configmap: Configmap) : Ctx {

    private val instances = mutableMapOf<Class<*>, Any>()

    // Configuration properties
    private val kafkaAdminProps: KafkaAdminProps
    private val schemaRegistryProps: SchemaRegistryProps
    private val schemasSpecProps: SchemasSpecProps
    private val topicsSpecProps: TopicsSpecProps
    private val sandboxProps: SandboxProps

    // Core services
    private val registryClient: RegistryClient
    private val adminClientsPool: AdminClientsPool
    private val topicOps: TopicOps
    private val topicService: TopicService
    private val schemaService: SchemaService

    init {
        log.debug("Initializing context...")

        // Load configuration properties
        kafkaAdminProps = configmap.get("kafkaAdmin", KafkaAdminProps::class.java)
        schemaRegistryProps = configmap.get("schemaRegistry", SchemaRegistryProps::class.java)
        schemasSpecProps = configmap.get("schemaSpecs", SchemasSpecProps::class.java)
        topicsSpecProps = configmap.get("topicSpecs", TopicsSpecProps::class.java)
        sandboxProps = configmap.get("sandbox", SandboxProps::class.java)


        registryClient = ConfluentSchemaRegistryClient(
            schemasSpecProps,
            sandboxProps,
            CachedSchemaRegistryClient(
                schemaRegistryProps.baseUrl,
                256,
                listOf(ProtobufSchemaProvider()),
                schemaRegistryProps.config
            )
        )


        // Initialize domain services with their dependencies
        adminClientsPool = AdminClientsPool(kafkaAdminProps)
        topicOps = TopicOps(topicsSpecProps, adminClientsPool, sandboxProps)
        topicService = TopicService(topicOps)

        val schemaDeps = SchemaDeps(sandboxProps)
        schemaService = SchemaService(registryClient, schemasSpecProps, schemaDeps)

        instances[TopicService::class.java] = topicService
        instances[SandboxProps::class.java] = sandboxProps
        instances[TopicOps::class.java] = topicOps
        instances[AdminClientsPool::class.java] = adminClientsPool
        instances[TopicsSpecProps::class.java] = topicsSpecProps
        instances[KafkaAdminProps::class.java] = kafkaAdminProps
        instances[SchemaRegistryProps::class.java] = schemaRegistryProps
        instances[SchemasSpecProps::class.java] = schemasSpecProps
        instances[RegistryClient::class.java] = registryClient
        instances[SchemaService::class.java] = schemaService
    }

    override fun <T> get(clazz: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return instances[clazz] as T? ?: throw IllegalArgumentException("No instance found for class: ${clazz.name}")
    }

    override fun close() {
        log.debug("Closing application context...")

        for (instance in instances.values) {
            if (instance is AutoCloseable) {
                try {
                    instance.close()
                } catch (e: Exception) {
                    log.error("Error closing instance of ${instance::class.java.name}: ${e.message}", e)
                }
            }
        }
    }
}
