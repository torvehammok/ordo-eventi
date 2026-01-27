package io.github.torvehammok.domain.schema

import de.danielbechler.diff.node.DiffNode
import java.nio.file.PathMatcher
import java.nio.file.Paths

data class SchemaUpdatePlan(val ops: List<SchemaOp>, val depsGraph: SchemasGraph, val pathMatcher: PathMatcher) {

    fun changes(): List<SchemaDef> {
        val changedSchemas = ops
            .filter { it.state != DiffNode.State.UNTOUCHED }
            .map { op -> op.schema.subject }

        return depsGraph
            .traverseSuccessors(changedSchemas)
            .filter { pathMatcher.matches(Paths.get(it.filename)) }
            .toList()
    }
}

data class SchemaOp(
    val state: DiffNode.State,
    val schema: RegistrySchema,
    val definitionChanged: Boolean = false,
    val refsChanged: Boolean = false,
)
