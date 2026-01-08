package io.github.torvehammok.infra

import org.testcontainers.kafka.ConfluentKafkaContainer


class CCKafkaContainer : ConfluentKafkaContainer("confluentinc/cp-kafka:8.0.3") {
    init {
        withNetworkAliases("broker")
        withListener("broker:29092")
    }
}

