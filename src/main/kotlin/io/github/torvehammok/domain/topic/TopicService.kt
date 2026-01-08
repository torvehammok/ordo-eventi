package io.github.torvehammok.domain.topic

import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger(TopicService::class.java)

class TopicService(private val topicOps: TopicOps) {

    fun applyTopicChanges() {
        log.info("Planning changes in kafka topics...")

        val plan = topicOps.planTopicUpdate()

        val summary = TopicDiffPrinter().printDiffs(plan)

        log.info("Executing topics changes plan:\n\n{}", summary)

        topicOps.executeTopicUpdatePlan(plan)
    }

    fun planTopicChanges(): TopicUpdatePlan {
        log.info("Planning changes in kafka topics...")

        val plan = topicOps.planTopicUpdate()

        val summary = TopicDiffPrinter().printDiffs(plan)

        log.info("Topics changes plan:\n\n{}", summary)
        return plan
    }

}