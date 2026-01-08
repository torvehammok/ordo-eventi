package io.github.torvehammok

import io.github.torvehammok.infra.CCKafkaContainer
import io.github.torvehammok.infra.CCSchemaRegistryContainer
import org.testcontainers.containers.Network
import org.testcontainers.kafka.ConfluentKafkaContainer

open class OrdoEventiTest {

    companion object {

        @JvmStatic
        val kafkaContainer: ConfluentKafkaContainer = CCKafkaContainer()

        @JvmStatic
        val schemaRegistryContainer = CCSchemaRegistryContainer()

        init {
            val network = Network.newNetwork()

            kafkaContainer
                .withNetwork(network)
                .start()

            schemaRegistryContainer
                .withNetwork(network)
                .dependsOn(kafkaContainer)
                .start()

            System.setProperty("KAFKA_BOOTSTRAP_SERVERS", kafkaContainer.bootstrapServers)
            System.setProperty("SCHEMA_REGISTRY_URL", schemaRegistryContainer.schemaRegistryUrl)
        }
    }

}
