package io.github.torvehammok.domain.schema

import de.danielbechler.diff.node.DiffNode

data class SchemaUpdatePlan(val ops: List<SchemaOp>, val depsGraph: SchemasGraph) {

    fun changes(): List<SchemaDef> {
        val changedSchemas = ops
            .filter { it.state != DiffNode.State.UNTOUCHED }
            .map { op -> op.schema.subject }

        return depsGraph.traverse(changedSchemas).toList()
    }
}

data class SchemaOp(
    val state: DiffNode.State,
    val schema: RegistrySchema,
    val definitionChanged: Boolean = false,
    val refsChanged: Boolean = false,
)
