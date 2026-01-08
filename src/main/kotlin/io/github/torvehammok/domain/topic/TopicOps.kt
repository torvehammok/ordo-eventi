package io.github.torvehammok.domain.topic

import de.danielbechler.diff.ObjectDiffer
import de.danielbechler.diff.ObjectDifferBuilder
import de.danielbechler.diff.node.DiffNode
import de.danielbechler.diff.node.DiffNode.State.ADDED
import de.danielbechler.diff.node.DiffNode.State.CHANGED
import de.danielbechler.diff.node.DiffNode.State.REMOVED
import io.github.torvehammok.cli.TopicSpecProps
import io.github.torvehammok.cli.TopicsSpecProps
import io.github.torvehammok.domain.sandbox.SandboxProps
import org.apache.kafka.clients.admin.*
import org.apache.kafka.clients.admin.AlterConfigOp.OpType.SET
import org.apache.kafka.common.config.ConfigResource
import org.apache.kafka.common.config.ConfigResource.Type.TOPIC
import org.slf4j.LoggerFactory
import java.util.Optional
import java.util.concurrent.TimeUnit

private val log = LoggerFactory.getLogger(TopicOps::class.java)

class TopicOps(
    private val topicsSpecProps: TopicsSpecProps,
    private val adminClientsPool: AdminClientsPool,
    private val sandboxProps: SandboxProps
) {

    fun executeTopicUpdatePlan(plan: TopicUpdatePlan) {
        adminClientsPool.withAdminClient { adminClient ->
            for (diff in plan.ops) {
                when (diff.diffNode.state) {
                    ADDED -> {
                        log.info("Creating topic: ${diff.expected?.name}")
                        val topicSpec = diff.expected!!

                        val newTopic = NewTopic(topicSpec.name, Optional.of(topicSpec.partitions), Optional.empty())

                        adminClient.createTopics(listOf(newTopic)).all().get(10, TimeUnit.SECONDS)

                        val updates = toTopicUpdate(diff)
                        if (updates.isNotEmpty()) {
                            adminClient
                                .incrementalAlterConfigs(mapOf(ConfigResource(TOPIC, diff.expected.name) to updates))
                                .all()
                                .get(10, TimeUnit.SECONDS)
                        }
                    }

                    CHANGED -> {
                        log.info("Updating topic: ${diff.expected?.name}")

                        val updates = toTopicUpdate(diff)
                        if (updates.isNotEmpty()) {
                            adminClient
                                .incrementalAlterConfigs(mapOf(ConfigResource(TOPIC, diff.expected!!.name) to updates))
                                .all()
                                .get(10, TimeUnit.SECONDS)
                        }

                        if (diff.diffNode.getChild("partitions")?.state == CHANGED) {
                            val expectedPartitions = diff.expected!!.partitions

                            adminClient
                                .createPartitions(
                                    mapOf(
                                        diff.expected.name to NewPartitions.increaseTo(
                                            expectedPartitions
                                        )
                                    )
                                )
                                .all()
                                .get(10, TimeUnit.SECONDS)
                        }
                    }

                    REMOVED -> {
                        log.info("Removing topic: ${diff.actual?.name}")
                        val actual = diff.actual!!
                        adminClient
                            .deleteTopics(listOf(actual.name))
                            .all()
                            .get(10, TimeUnit.SECONDS)
                    }

                    else -> {
                        // do nothing
                    }
                }
            }
        }
    }

    fun planTopicUpdate(): TopicUpdatePlan {
        val expectedTopicSpecs = topicsSpecProps.topics.map {
            it.copy(name = toTopicName(it.name, sandboxProps)).merge(topicsSpecProps.topicDefaults)
        }
        val currentTopicSpecs = findCurrentTopicSpecs()

        val differ = createObjectDiffer()

        val diffs = mutableListOf<TopicDiff>()

        for (expected in expectedTopicSpecs) {
            val actual = currentTopicSpecs.find { it.name == expected.name }

            if (expected.deleteTopic && actual == null) {
                // Topic is marked for deletion but does not exist, skip
                continue
            }

            val diffNode = differ.compare(
                if (expected.deleteTopic) null else expected,
                actual
            )

            diffs.add(
                TopicDiff(
                    expected = expected,
                    actual = actual,
                    diffNode = diffNode
                )
            )
        }

        return TopicUpdatePlan(diffs)
    }

    private fun toTopicUpdate(diff: TopicDiff): List<AlterConfigOp> {
        val ops = mutableListOf<AlterConfigOp>()
        diff.diffNode.getChild("configOverrides").visitChildren { childNode, _ ->
            val key = childNode.path.lastElementSelector.toHumanReadableString()
                .replace("{", "")
                .replace("}", "")

            when (childNode.state) {
                ADDED, CHANGED -> {
                    val value = childNode.canonicalGet(diff.expected) as String
                    ops.add(AlterConfigOp(ConfigEntry(key, value), SET))
                }

                REMOVED -> {
                    val key = childNode.path.lastElementSelector.toHumanReadableString()
                        .replace("{", "")
                        .replace("}", "")
                    ops.add(AlterConfigOp(ConfigEntry(key, null), AlterConfigOp.OpType.DELETE))
                }

                else -> {
                    // do nothing
                }
            }
        }

        return ops
    }

    private fun findCurrentTopicSpecs(): List<TopicSpecProps> {
        return adminClientsPool.withAdminClient { adminClient ->
            val topics = adminClient
                .listTopics(ListTopicsOptions().listInternal(false))
                .names()
                .get(10, TimeUnit.SECONDS)
                .filter {
                    if (sandboxProps.enabled) {
                        it.startsWith(sandboxProps.prefix)
                    } else {
                        true
                    }
                }

            val topicDescriptions = adminClient.describeTopics(topics).allTopicNames().get(10, TimeUnit.SECONDS)

            val topicConfigs = adminClient
                .describeConfigs(topics.map { topicName -> ConfigResource(TOPIC, topicName) })
                .all()
                .get(10, TimeUnit.SECONDS)

            val currentTopicSpecs = topicDescriptions.map { (name, desc) ->
                val configResource = ConfigResource(TOPIC, name)
                val config = topicConfigs[configResource]

                TopicSpecProps(
                    name = name,
                    partitions = desc.partitions().size,
                    configOverrides = config?.entries()
                        ?.filter { entry -> entry.source() != ConfigEntry.ConfigSource.DEFAULT_CONFIG }
                        ?.associate { it.name() to it.value() } ?: emptyMap()
                )
            }

            currentTopicSpecs
        }
    }
}

private fun createObjectDiffer(): ObjectDiffer {
    return ObjectDifferBuilder.startBuilding()
        .filtering()
        .returnNodesWithState(DiffNode.State.UNTOUCHED, true)
        .and()
        .inclusion()
        .exclude()
        .propertyNameOfType(TopicSpecProps::class.java, "name")
        .propertyNameOfType(TopicSpecProps::class.java, "deleteTopic")
        .and()
        .build()
}

private fun toTopicName(topicName: String, sandboxProps: SandboxProps): String {
    return if (sandboxProps.enabled) {
        sandboxProps.prefix + topicName
    } else {
        topicName
    }
}