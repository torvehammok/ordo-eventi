package io.github.torvehammok.infra.ctx

data class KafkaAdminProps(
    var bootstrapServers: String = "localhost:9092",
    var config: Map<String, String> = emptyMap()
)
