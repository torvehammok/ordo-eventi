@file:Suppress("UnstableApiUsage")

package io.github.torvehammok.domain.schema

import com.google.common.graph.ElementOrder
import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph
import io.github.torvehammok.domain.schema.registry.RegistrySchemaDiscoveryStrategy

class SchemaDeps(private val schemasDiscoveryStrategy: SchemasDiscoveryStrategy) {

    fun resolveSchemaDeps(): SchemasGraph {
        return discoverSchemasWithStrategy(schemasDiscoveryStrategy)
    }

    fun resolveSchemaDeps(schemas: List<RegistrySchema>): SchemasGraph {
        val strategy = RegistrySchemaDiscoveryStrategy(schemas)

        return discoverSchemasWithStrategy(strategy)
    }

}

private fun discoverSchemasWithStrategy(strategy: SchemasDiscoveryStrategy): SchemasGraph {
    val graph = createDependenciesGraph()

    val discoveredSchemas = strategy.discoverSchemas()

    discoveredSchemas.forEach { discoveredSchema ->
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
