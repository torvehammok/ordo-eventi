package io.github.torvehammok.cli

import io.github.torvehammok.Ctx
import io.github.torvehammok.domain.schema.SchemaService
import io.github.torvehammok.infra.config.readYamlFileConfigmap
import io.github.torvehammok.infra.ctx.DefaultCtx
import picocli.CommandLine
import picocli.CommandLine.Command

@Command(
    name = "schemas-plan",
    description = ["Plans schema changes based on the configuration."],
    mixinStandardHelpOptions = true
)
class SchemasPlanCommand : Runnable {

    @CommandLine.Option(
        names = ["-i", "--inclusion-globs"],
        description = ["List of glob patterns of files to include (if not specified, configuration is taken from the config map)."]
    )
    private var inclusionGlobs: List<String>? = null

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
        val schemaService = ctx.get(SchemaService::class.java)

        schemaService.planSchemaUpdates(
            inclusionGlobs = inclusionGlobs
        )
    }
}

