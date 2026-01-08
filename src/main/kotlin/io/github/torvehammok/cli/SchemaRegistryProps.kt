package io.github.torvehammok.cli

data class SchemaRegistryProps(
    var baseUrl: String = "",
    var config: Map<String, String> = emptyMap(),
)
