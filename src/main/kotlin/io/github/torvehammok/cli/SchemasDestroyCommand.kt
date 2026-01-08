package io.github.torvehammok.cli

import io.github.torvehammok.Ctx
import io.github.torvehammok.domain.schema.SchemaService
import io.github.torvehammok.infra.config.readYamlFileConfigmap
import io.github.torvehammok.infra.ctx.DefaultCtx
import picocli.CommandLine
import picocli.CommandLine.Command

@Command(
    name = "schemas-destroy",
    description = ["Destroys all schemas managed by the configuration."],
    mixinStandardHelpOptions = true
)
class SchemasDestroyCommand : Runnable {

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
        schemaService.destroySchemas()
    }
}

