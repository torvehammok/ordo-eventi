package io.github.torvehammok.domain

import io.github.torvehammok.OrdoEventiTest
import io.github.torvehammok.domain.topic.AdminClientsPool
import io.github.torvehammok.domain.topic.TopicDiffPrinter
import io.github.torvehammok.domain.topic.TopicService
import io.github.torvehammok.infra.config.YamlFileConfigmap
import io.github.torvehammok.infra.ctx.DefaultCtx
import org.apache.kafka.clients.admin.NewTopic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TopicServiceTest : OrdoEventiTest() {

    companion object {

        private lateinit var adminClientPool: AdminClientsPool
        private lateinit var topicService: TopicService

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            val configmap = YamlFileConfigmap(ClassLoader.getSystemResource("configmap-test.yaml").openStream())

            val ctx = DefaultCtx(configmap)
            topicService = ctx.get(TopicService::class.java)
            adminClientPool = ctx.get(AdminClientsPool::class.java)
        }

    }

    @BeforeEach
    fun setUp() {
        adminClientPool.withAdminClient { adminClient ->
            // delete all topics
            val topics = adminClient.listTopics().names().get()

            if (topics.isNotEmpty()) {
                adminClient.deleteTopics(topics).all().get()
                Thread.sleep(200)
            }
        }
    }

    @Test
    fun testCreateTopics() {
        val plan = topicService.planTopicChanges()

        val printedDiff = TopicDiffPrinter().printDiffs(plan)

        assertThat(printedDiff).isEqualTo(
            """
            + Topic 'private.topic-1' will be created:
                + configOverrides:
                    + cleanup.policy=delete
                    + retention.ms=1209600000
                + partitions=3
            + Topic 'private.topic-2' will be created:
                + configOverrides:
                    + cleanup.policy=delete
                    + retention.ms=1209600000
                + partitions=3
            + Topic 'private.topic-3' will be created:
                + configOverrides:
                    + cleanup.policy=delete
                    + retention.ms=1209600000
                + partitions=3

            """.trimIndent()
        )
    }

    @Test
    fun testUpdateTopics() {
        adminClientPool.withAdminClient { adminClient ->
            adminClient.createTopics(listOf(NewTopic("private.topic-2", 3, 1))).all().get()
        }

        val plan = topicService.planTopicChanges()

        val printedDiff = TopicDiffPrinter().printDiffs(plan)

        assertThat(printedDiff).isEqualTo(
            """
            + Topic 'private.topic-1' will be created:
                + configOverrides:
                    + cleanup.policy=delete
                    + retention.ms=1209600000
                + partitions=3
            + Topic 'private.topic-3' will be created:
                + configOverrides:
                    + cleanup.policy=delete
                    + retention.ms=1209600000
                + partitions=3
            
            ~ Topic 'private.topic-2' will be updated:
                  partitions=3
                ~ configOverrides:
                    + cleanup.policy=delete
                    + retention.ms=1209600000
                    - flush.messages=9223372036854775807
            
            
            """.trimIndent()
        )
    }

    @Test
    fun testDeleteOldTopic() {
        adminClientPool.withAdminClient { adminClient ->
            adminClient
                .createTopics(
                    listOf(
                        NewTopic("private.topic-2", 3, 1),
                        NewTopic("private.topic-4", 3, 1)
                    )
                )
                .all()
                .get()
        }

        val plan = topicService.planTopicChanges()

        val printedPlan = TopicDiffPrinter().printDiffs(plan)

        assertThat(printedPlan).isEqualTo(
            """
            + Topic 'private.topic-1' will be created:
                + configOverrides:
                    + cleanup.policy=delete
                    + retention.ms=1209600000
                + partitions=3
            + Topic 'private.topic-3' will be created:
                + configOverrides:
                    + cleanup.policy=delete
                    + retention.ms=1209600000
                + partitions=3
            
            ~ Topic 'private.topic-2' will be updated:
                  partitions=3
                ~ configOverrides:
                    + cleanup.policy=delete
                    + retention.ms=1209600000
                    - flush.messages=9223372036854775807
            
            
            - Topic 'private.topic-4' will be deleted:
                - configOverrides:
                    - flush.messages=9223372036854775807
                - partitions=3

          """.trimIndent()
        )
    }
}
