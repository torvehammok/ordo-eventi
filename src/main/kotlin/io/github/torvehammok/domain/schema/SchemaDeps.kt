@file:Suppress("UnstableApiUsage")

package io.github.torvehammok.domain.schema

import com.google.common.graph.ElementOrder
import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph
import io.github.torvehammok.domain.sandbox.SandboxProps
import io.github.torvehammok.domain.schema.avro.AvroSchemasDiscoveryStrategy
import io.github.torvehammok.domain.schema.proto.ProtoSchemasDiscoveryStrategy
import io.github.torvehammok.domain.schema.registry.RegistrySchemaDiscoveryStrategy
import java.nio.file.Path
import java.nio.file.PathMatcher

class SchemaDeps(private val sandboxProps: SandboxProps) {

    fun resolveSchemaDeps(schemasDir: Path, matcher: PathMatcher = PathMatcher { true }): SchemasGraph {
        val strategy = ProtoSchemasDiscoveryStrategy(sandboxProps, schemasDir)

        return discoverSchemasWithStrategy(strategy, matcher)
    }

    fun resolveAvroSchemaDeps(schemasDir: Path, matcher: PathMatcher = PathMatcher { true }): SchemasGraph {
        val strategy = AvroSchemasDiscoveryStrategy(sandboxProps, schemasDir)

        return discoverSchemasWithStrategy(strategy, matcher)
    }

    fun resolveSchemaDeps(schemas: List<RegistrySchema>): SchemasGraph {
        val strategy = RegistrySchemaDiscoveryStrategy(schemas)

        return discoverSchemasWithStrategy(strategy) { true }
    }

}

private fun discoverSchemasWithStrategy(strategy: SchemasDiscoveryStrategy, matcher: PathMatcher): SchemasGraph {
    val graph = createDependenciesGraph()

    strategy.discoverSchemas(matcher).forEach { discoveredSchema ->
        graph.addNode(discoveredSchema.def)

        for (ref in discoveredSchema.refs) {
            graph.putEdge(ref, discoveredSchema.def)
        }
    }

    return GuavaSchemasGraph(graph)
}

private fun createDependenciesGraph(): MutableGraph<SchemaDef> {
    return GraphBuilder.directed()
        .nodeOrder(
            ElementOrder.sorted(Comparator.comparing<SchemaDef, String> { it.subject })
        )
        .build()
}
