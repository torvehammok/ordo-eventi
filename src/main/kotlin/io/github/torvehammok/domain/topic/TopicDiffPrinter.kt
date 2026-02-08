package io.github.torvehammok.domain.topic

import de.danielbechler.diff.node.DiffNode
import java.time.temporal.Temporal

class TopicDiffPrinter {

    fun printDiffs(plan: TopicUpdatePlan): String {
        val builder = StringBuilder()

        val unchanged = mutableListOf<TopicDiff>()
        val added = mutableListOf<TopicDiff>()
        val changed = mutableListOf<TopicDiff>()
        val removed = mutableListOf<TopicDiff>()

        for (diff in plan.ops.sortedBy { it.actual?.name ?: it.expected?.name }) {
            when (diff.diffNode.state) {
                DiffNode.State.UNTOUCHED -> unchanged.add(diff)
                DiffNode.State.ADDED -> added.add(diff)
                DiffNode.State.CHANGED -> changed.add(diff)
                DiffNode.State.REMOVED -> removed.add(diff)
                else -> {
                    // do nothing
                }
            }
        }

        for (diff in unchanged) {
            builder.append("# Topic '${diff.expected?.name}' is in sync\n")
        }

        if (added.isNotEmpty() || changed.isNotEmpty() || removed.isNotEmpty()) {
            builder.append("\n")
        }

        for (diff in added) {
            builder.append("+ Topic '${diff.expected?.name}' will be created:\n")
            builder.append(printNode(diff.diffNode, diff.expected, diff.actual))
            builder.append('\n')
        }

        if (changed.isNotEmpty() && builder.isNotEmpty()) {
            builder.append("\n")
        }

        for (diff in changed) {
            builder.append("~ Topic '${diff.expected?.name}' will be updated:\n")
            builder.append(printNode(diff.diffNode, diff.expected, diff.actual))
            builder.append('\n')
        }

        if (removed.isNotEmpty() && builder.isNotEmpty()) {
            builder.append("\n")
        }

        for (diff in removed) {
            builder.append("- Topic '${diff.actual?.name}' will be deleted:\n")
            builder.append(printNode(diff.diffNode, diff.expected, diff.actual))
        }

        return builder.toString()
    }

    private fun printNode(node: DiffNode, expected: Any?, actual: Any?): String {
        val builder = StringBuilder()

        val childNodes = mutableListOf<DiffNode>()
        node.visitChildren { childNode, visit ->
            childNodes.add(childNode)
            visit.dontGoDeeper()
        }
        childNodes.sortWith { a, b ->
            return@sortWith childNodesComparator(a, b)
        }

        for (childNode in childNodes) {
            val expectedValue = childNode.canonicalGet(expected)
            val actualValue = childNode.canonicalGet(actual)

            val comparedValue = expectedValue ?: actualValue

            if (!isPrimitive(comparedValue)) {
                when (childNode.state) {
                    DiffNode.State.ADDED -> builder
                        .append("    ".repeat(childNode.path.elementSelectors.size - 1))
                        .append("+ ${childNode.propertyName}:\n")

                    DiffNode.State.REMOVED -> builder
                        .append("    ".repeat(childNode.path.elementSelectors.size - 1))
                        .append("- ${childNode.propertyName}:\n")

                    DiffNode.State.CHANGED -> builder
                        .append("    ".repeat(childNode.path.elementSelectors.size - 1))
                        .append("~ ${childNode.propertyName}:\n")

                    DiffNode.State.UNTOUCHED -> builder
                        .append("    ".repeat(childNode.path.elementSelectors.size - 1))
                        .append("  ${childNode.propertyName}:\n")

                    else -> {
                        // do nothing
                    }
                }
                builder.append(printNode(childNode, expected, actual))
                continue
            }

            val toHumanReadableString = childNode.path.lastElementSelector.toHumanReadableString()
                .replace("{", "")
                .replace("}", "")

            when (childNode.state) {
                DiffNode.State.ADDED -> builder
                    .append("    ".repeat(childNode.path.elementSelectors.size - 1))
                    .append("+ $toHumanReadableString=$expectedValue\n")

                DiffNode.State.REMOVED -> builder
                    .append("    ".repeat(childNode.path.elementSelectors.size - 1))
                    .append("- $toHumanReadableString=$actualValue\n")

                DiffNode.State.CHANGED -> builder
                    .append("    ".repeat(childNode.path.elementSelectors.size - 1))
                    .append("~ $toHumanReadableString=$actualValue -> $expectedValue\n")

                DiffNode.State.UNTOUCHED -> builder
                    .append("    ".repeat(childNode.path.elementSelectors.size - 1))
                    .append("  $toHumanReadableString=$expectedValue\n")

                else -> {
                    // do nothing
                }
            }
        }

        return builder.toString()
    }

}

private fun childNodesComparator(a: DiffNode, b: DiffNode): Int {
    val state1 = diffStateToInt(a.state)
    val state2 = diffStateToInt(b.state)

    if (state1 == state2) {
        return a.propertyName.compareTo(b.propertyName)
    }

    return state1 - state2
}

private fun isPrimitive(comparedValue: Any?): Boolean {
    return comparedValue == null ||
            comparedValue is String ||
            comparedValue is Number ||
            comparedValue is Boolean ||
            comparedValue is Temporal
}

fun diffStateToInt(diffState: DiffNode.State): Int {
    return when (diffState) {
        DiffNode.State.UNTOUCHED -> 0
        DiffNode.State.ADDED -> 1
        DiffNode.State.CHANGED -> 2
        DiffNode.State.REMOVED -> 3
        else -> 4
    }
}