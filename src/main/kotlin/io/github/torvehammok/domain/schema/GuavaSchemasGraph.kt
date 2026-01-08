@file:Suppress("UnstableApiUsage")

package io.github.torvehammok.domain.schema

import com.google.common.graph.Graph
import com.google.common.graph.Traverser

class GuavaSchemasGraph(private val graph: Graph<SchemaDef>) : SchemasGraph {

    override fun traverse(changedSchemas: List<String>): List<SchemaDef> {
        val schemas = graph.nodes()

        val changedSchemasNodes = changedSchemas.map { schemas.find { schema -> it == schema.subject } }
        return Traverser.forGraph(graph).depthFirstPostOrder(changedSchemasNodes).reversed()
    }

    override fun dependencyGraph(namespace: String?): String {
        val builder = StringBuilder()

        val namespaces =
            if (namespace == null)
                graph.nodes().map { it.packageName }.distinct()
            else listOf(namespace)

        for (namespace in namespaces) {
            val startingPoints = graph.nodes()
                .filter { it.packageName == namespace }
                .filter { graph.predecessors(it).isEmpty() }
                .sortedBy { it.filename }

            builder.append("---\n")
            builder.append("namespace: $namespace\n")
            builder.append("schemas:\n")

            Traverser.forGraph(graph).depthFirstPostOrder(startingPoints).reversed().forEach {
                if (it.packageName != namespace) {
                    return@forEach
                }

                builder.append("  - name: ${it.filename}\n")
                val successors = graph.predecessors(it).sortedBy { node -> node.subject }

                if (successors.isNotEmpty()) {
                    builder.append("    deps:\n")
                    for (successor in successors) {
                        builder.append("      - name: ${successor.filename}\n")
                        if (successor.packageName != it.packageName) {
                            builder.append("        namespace: ${successor.packageName}\n")
                        }
                    }
                }
            }
        }


        return builder.toString()
    }

    override fun dependencyGraphItems(): List<SchemaDef> {
        val startingPoints = graph.nodes()
            .filter { graph.predecessors(it).isEmpty() }
            .sortedBy { it.subject }

        return Traverser.forGraph(graph).depthFirstPostOrder(startingPoints).toList()
    }

    override fun listDeps(subject: String): List<SchemaDef> {
        val schema = graph.nodes().find { it.subject == subject }!!
        return graph.predecessors(schema).toList()
    }

    override fun allSchemas(): Set<SchemaDef> {
        return graph.nodes()
    }
}