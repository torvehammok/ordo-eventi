package io.github.torvehammok.domain.schema

import java.nio.file.PathMatcher

interface SchemasDiscoveryStrategy {
    fun discoverSchemas(pathMatcher: PathMatcher): List<DiscoveredSchema>
}

data class DiscoveredSchema(
    val def: SchemaDef,
    val refs: List<SchemaDef>
)
