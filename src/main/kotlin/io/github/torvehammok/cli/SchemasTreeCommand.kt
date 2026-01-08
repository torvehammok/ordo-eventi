package io.github.torvehammok.cli

import io.github.torvehammok.domain.schema.SchemaService
import io.github.torvehammok.Ctx
import io.github.torvehammok.domain.schema.SchemasNamespacesPrinter
import io.github.torvehammok.infra.config.readYamlFileConfigmap
import io.github.torvehammok.infra.ctx.DefaultCtx
import org.slf4j.LoggerFactory
import picocli.CommandLine
import picocli.CommandLine.Command

private val log = LoggerFactory.getLogger(SchemasTreeCommand::class.java)

@Command(
    name = "schemas-tree",
    description = ["Displays the schema tree based on the configuration."],
    mixinStandardHelpOptions = true
)
class SchemasTreeCommand : Runnable {

    @CommandLine.Option(
        names = ["-n", "--namespace"],
        description = ["The namespace to operate on (if not specified, all namespaces are considered)."]
    )
    private var namespace: String? = null

    @CommandLine.Option(
        names = ["-c", "--configmap"],
        description = ["Path to the configmap YAML file (if not specified, the default configmap.yaml from resources is used)."]
    )
    private var configLocation: String? = null

    override fun run() {
        val cm = readYamlFileConfigmap(configLocation)

        DefaultCtx(cm).use { ctx ->
            printSchemasNamespaces(ctx)
        }
    }

    private fun printSchemasNamespaces(ctx: Ctx) {
        val schemaService = ctx.get(SchemaService::class.java)
        val namespaces = schemaService.listSchemasNamespaces(namespace = namespace)

        log.info("Schemas dependencies:\n\n{}", SchemasNamespacesPrinter().print(namespaces))
    }
}

