@file:Suppress("UnstableApiUsage")

package io.github.torvehammok.domain.schema

import com.google.common.graph.ElementOrder
import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph
import io.github.torvehammok.cli.SchemasSpecFormat.*
import io.github.torvehammok.cli.SchemasSpecProps
import io.github.torvehammok.domain.sandbox.SandboxProps
import io.github.torvehammok.domain.schema.avro.AvroSchemasDiscoveryStrategy
import io.github.torvehammok.domain.schema.proto.ProtoSchemasDiscoveryStrategy
import io.github.torvehammok.domain.schema.registry.RegistrySchemaDiscoveryStrategy
import java.nio.file.Paths

class SchemaDeps(
    private val sandboxProps: SandboxProps,
    private val schemasSpecProps: SchemasSpecProps
) {

    fun resolveSchemaDeps(): SchemasGraph {
        val schemasDir = Paths.get(schemasSpecProps.dir)

        val strategy = when (schemasSpecProps.format) {
            AVRO -> AvroSchemasDiscoveryStrategy(sandboxProps, schemasDir)
            PROTOBUF -> ProtoSchemasDiscoveryStrategy(sandboxProps, schemasDir)
        }

        return discoverSchemasWithStrategy(strategy)
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
