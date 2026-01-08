package io.github.torvehammok

import io.github.cdimascio.dotenv.dotenv
import io.github.torvehammok.cli.RootCommand
import picocli.CommandLine

fun main(args: Array<String>) {
    dotenv { systemProperties = true }
    CommandLine(RootCommand()).execute(*args)
}
