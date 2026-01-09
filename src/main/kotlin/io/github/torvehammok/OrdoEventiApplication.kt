package io.github.torvehammok

import io.github.cdimascio.dotenv.dotenv
import io.github.torvehammok.cli.RootCommand
import picocli.CommandLine

fun main(args: Array<String>) {
    dotenv {
        systemProperties = true
        ignoreIfMissing = true
    }
    CommandLine(RootCommand()).execute(*args)
}
