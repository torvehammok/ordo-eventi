package io.github.torvehammok.domain.topic

import de.danielbechler.diff.node.DiffNode
import io.github.torvehammok.cli.TopicSpecProps

data class TopicDiff(
    val expected: TopicSpecProps?,
    val actual: TopicSpecProps?,
    val diffNode: DiffNode
)