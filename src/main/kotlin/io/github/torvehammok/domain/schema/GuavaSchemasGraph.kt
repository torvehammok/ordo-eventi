@file:Suppress("UnstableApiUsage")

package io.github.torvehammok.domain.schema

import com.google.common.graph.Graph
import com.google.common.graph.SuccessorsFunction
import com.google.common.graph.Traverser

class GuavaSchemasGraph(private val graph: Graph<SchemaDef>) : SchemasGraph {

    override fun traversePredecessors(subject: String): List<SchemaDef> {
        val schemas = graph.nodes()

        val changedSchemasNodes = schemas.filter { it.subject == subject }

        return Traverser
            .forGraph(sortedPredecessorsFn(graph))
            .depthFirstPostOrder(changedSchemasNodes)
            .filter { it.subject != subject }
    }

    override fun traverseSuccessors(subjects: List<String>): List<SchemaDef> {
        val schemas = graph.nodes()

        val changedSchemasNodes = subjects.map { schemas.find { schema -> it == schema.subject } }

        return Traverser
            .forGraph(sortedSuccessorsFn(graph))
            .depthFirstPostOrder(changedSchemasNodes).reversed()
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

            Traverser
                .forGraph(sortedSuccessorsFn(graph))
                .depthFirstPostOrder(startingPoints)
                .reversed()
                .forEach {
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

    override fun traverseWholeGraph(): List<SchemaDef> {
        val startingPoints = graph.nodes()
            .filter { graph.predecessors(it).isEmpty() }
            .sortedBy { it.subject }

        return Traverser
            .forGraph(sortedSuccessorsFn(graph))
            .depthFirstPostOrder(startingPoints)
            .toList()
    }

    override fun findDirectPredecessors(subject: String): List<SchemaDef> {
        val schema = graph.nodes().find { it.subject == subject }!!
        return graph.predecessors(schema).toList()
    }

    override fun allSchemas(): Set<SchemaDef> {
        return graph.nodes()
    }
}

private fun sortedSuccessorsFn(graph: Graph<SchemaDef>): SuccessorsFunction<SchemaDef> {
    return SuccessorsFunction<SchemaDef> { node ->
        graph.successors(node).sortedBy { it.subject }
    }
}

private fun sortedPredecessorsFn(graph: Graph<SchemaDef>): SuccessorsFunction<SchemaDef> {
    return SuccessorsFunction<SchemaDef> { node ->
        graph.predecessors(node).sortedBy { it.subject }
    }
}
