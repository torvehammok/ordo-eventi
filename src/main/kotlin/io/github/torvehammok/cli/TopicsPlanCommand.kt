package io.github.torvehammok.cli

import io.github.torvehammok.Ctx
import io.github.torvehammok.domain.topic.TopicService
import io.github.torvehammok.infra.config.readYamlFileConfigmap
import io.github.torvehammok.infra.ctx.DefaultCtx
import picocli.CommandLine
import picocli.CommandLine.Command

@Command(
    name = "topics-plan",
    description = ["Plans Kafka topic changes based on the configuration."],
    mixinStandardHelpOptions = true
)
class TopicsPlanCommand : Runnable {

    @CommandLine.Option(
        names = ["-c", "--configmap"],
        description = ["Path to the configmap YAML file (if not specified, the default configmap.yaml from resources is used)."]
    )
    private var configLocation : String? = null

    override fun run() {
        val cm = readYamlFileConfigmap(configLocation)

        DefaultCtx(cm).use { ctx ->
            plan(ctx)
        }
    }

    private fun plan(ctx: Ctx) {
        val topicService = ctx.get(TopicService::class.java)
        topicService.planTopicChanges()
    }
}

