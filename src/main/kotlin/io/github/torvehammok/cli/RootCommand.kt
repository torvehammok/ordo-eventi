package io.github.torvehammok.cli

import org.slf4j.LoggerFactory
import picocli.CommandLine.Command

private val log = LoggerFactory.getLogger(RootCommand::class.java)

@Command(
    subcommands = [
        TopicsPlanCommand::class,
        TopicsApplyCommand::class,
        SchemasPlanCommand::class,
        SchemasApplyCommand::class,
        SchemasTreeCommand::class,
        SchemasDestroyCommand::class
    ]
)
class RootCommand : Runnable {
    override fun run() {
        log.info("Use a subcommand. Use --help to see available commands.")
    }
}

