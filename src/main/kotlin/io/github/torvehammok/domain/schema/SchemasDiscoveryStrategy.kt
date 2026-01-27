package io.github.torvehammok.domain.schema

interface SchemasDiscoveryStrategy {
    fun discoverSchemas(): List<DiscoveredSchema>
}

data class DiscoveredSchema(
    val def: SchemaDef,
    val refs: List<SchemaDef>
)
