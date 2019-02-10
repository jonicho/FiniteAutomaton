package de.jrk.finiteautomaton

import org.json.JSONObject

object Parser {
    /**
     * Parses the given [json] string to a [FiniteAutomaton].
     * The [json] string has to represent a valid [FiniteAutomaton].
     */
    fun parse(json: String): FiniteAutomaton {
        val jsonObject = JSONObject(json)
        val finiteAutomaton =
            FiniteAutomaton(jsonObject.getString("alphabet").toList(), jsonObject.getBoolean("forceDeterminism"))
        jsonObject.getJSONArray("states").map { it as JSONObject }.forEach {
            finiteAutomaton.addState(
                it.getString("name"),
                it.optBoolean("accepting"),
                it.optBoolean("initial")
            )
        }
        jsonObject.getJSONArray("transitions").map { it as JSONObject }.forEach {
            finiteAutomaton.addTransition(
                it.getString("startState"),
                it.getString("targetState"),
                it.getString("inputCharacters").toList()
            )
        }
        return finiteAutomaton
    }
}
