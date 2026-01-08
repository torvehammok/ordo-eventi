package io.github.torvehammok.domain.topic

data class TopicUpdatePlan(val ops: List<TopicDiff>)