package de.jrk.finiteautomaton.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

class FiniteAutomatonCommand : CliktCommand(name = "finiteAutomaton") {
    override fun run() = Unit
}

fun main(args: Array<String>) {
    FiniteAutomatonCommand().subcommands(CheckStringCommand()).main(args)
}
