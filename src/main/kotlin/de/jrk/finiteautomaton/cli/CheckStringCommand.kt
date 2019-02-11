package de.jrk.finiteautomaton.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import de.jrk.finiteautomaton.Parser
import java.nio.file.Files
import kotlin.system.exitProcess

class CheckStringCommand :
    CliktCommand(name = "checkstring", help = "Checks whether the given automaton accepts the given string") {
    val automatonFile by argument(help = "The automaton json file").path(
        exists = true,
        folderOkay = false,
        readable = true
    )
    val string by argument(help = "The string to check")
    val boolean by option("-b", "--boolean", help = "Only show a boolean value instead of a sentence").flag()

    override fun run() {
        val jsonString = try {
            Files.readString(automatonFile)
        } catch (e: Exception) {
            echo("Error while reading file: ${e.message}", err = true)
            exitProcess(-1)
        }
        val finiteAutomaton = try {
            Parser.parse(jsonString)
        } catch (e: Exception) {
            echo("Error while parsing automaton: ${e.message}", err = true)
            exitProcess(-1)
        }
        val accepted = try {
            finiteAutomaton.checkString(string)
        } catch (e: Throwable) {
            echo("Error while checking string: ${e.message}", err = true)
            exitProcess(-1)
        }
        if (boolean) {
            echo(accepted)
        } else {
            echo("The string is ${if (!accepted) "not " else ""}accepted by the automaton.")
        }
    }
}
