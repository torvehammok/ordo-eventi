package io.github.torvehammok.domain.schema

import de.danielbechler.diff.node.DiffNode

class SchemaUpdatePlanPrinter {
    fun print(plan: SchemaUpdatePlan): String {
        val builder = StringBuilder()

        plan.ops
            .filter { op -> op.state == DiffNode.State.UNTOUCHED }
            .sortedBy { it.schema.subject }
            .forEach {
                builder.append("  # Schema '${it.schema.subject}' is in sync\n")
            }

        val changes = plan.changes()

        if (changes.isNotEmpty()) {
            builder.append('\n')
        }

        for (schema in changes) {
            val op = plan.ops.find { it.schema.subject == schema.subject }

            if (op == null || op.state == DiffNode.State.UNTOUCHED) {
                builder.append("    ~ Schema '${schema.subject}' refs will be updated due to upstream\n")
                continue
            }

            when (op.state) {

                DiffNode.State.ADDED -> {
                    builder.append("  + Schema '${op.schema.subject} needs to be created\n")
                }

                DiffNode.State.CHANGED -> {
                    val line = listOf(
                        if (op.definitionChanged) "definition" else "",
                        if (op.refsChanged) "references" else ""
                    ).filter { it.isNotEmpty() }.joinToString(" & ")

                    builder.append("  ~ Schema '${op.schema.subject}' needs to be updated due to changes in $line\n")
                }

                else -> {
                    // noop
                }
            }
        }

        return builder.toString()
    }

}
