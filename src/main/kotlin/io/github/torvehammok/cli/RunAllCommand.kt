package io.github.torvehammok.cli

import org.slf4j.LoggerFactory
import picocli.CommandLine
import picocli.CommandLine.Command

private val log = LoggerFactory.getLogger(RunAllCommand::class.java)

@Command(
    name = "run-all",
    description = ["Runs all planning and applying commands in sequence."],
    mixinStandardHelpOptions = true
)
class RunAllCommand : Runnable {

    @CommandLine.Parameters(
        index = "0..*",
        description = ["List of commands to run in sequence (e.g., topics-plan, topics-apply, schemas-plan, schemas-apply)."]
    )
    private var commands = listOf<String>()

    @CommandLine.Option(
        names = ["-c", "--configmap"],
        description = ["Path to the configmap YAML file (if not specified, the default configmap.yaml from resources is used)."]
    )
    private var configLocation: String? = null

    override fun run() {
        for (cmd in commands) {
            log.info("Executing command: {}", cmd)

            val arguments = mutableListOf(cmd)
            if (configLocation != null) {
                arguments.addAll(listOf("-c", configLocation!!))
            }

            CommandLine(RootCommand()).execute(*arguments.toTypedArray())
        }
    }

}
