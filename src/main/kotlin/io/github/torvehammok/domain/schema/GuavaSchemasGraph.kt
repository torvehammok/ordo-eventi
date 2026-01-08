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

    override fun listNamespaces(namespace: String?): List<NamespaceSchemas> {
        val result = mutableListOf<NamespaceSchemas>()

        val namespaces = if (namespace == null)
            graph.nodes().map { it.packageName }.distinct()
        else
            listOf(namespace)

        for (namespace in namespaces) {
            val startingPoints = graph.nodes()
                .filter { it.packageName == namespace }
                .filter { graph.predecessors(it).isEmpty() }
                .sortedBy { it.filename }

            val items = mutableListOf<NamespaceSchemas.Item>()

            Traverser.forGraph(graph).depthFirstPostOrder(startingPoints).reversed().forEach {
                if (it.packageName != namespace) {
                    return@forEach
                }

                val successors = graph.predecessors(it).sortedBy { node -> node.subject }

                items.add(
                    NamespaceSchemas.Item(
                        name = it.filename,
                        deps = successors.map { succ ->
                            NamespaceSchemas.Dep(
                                name = succ.filename,
                                namespace = if (succ.packageName != it.packageName) succ.packageName else null
                            )
                        }
                    )
                )
            }

            result.add(NamespaceSchemas(namespace = namespace, schemas = items))
        }

        return result
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