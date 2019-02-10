package de.jrk.finiteautomaton

import org.json.JSONArray
import org.json.JSONObject

object Serializer {
    /**
     * Serializes the given [finiteAutomaton] to a json string.
     * If [indent] is ```true``` the json string is indented with 4 spaces.
     */
    fun serialize(finiteAutomaton: FiniteAutomaton, indent: Boolean = false): String {
        val jsonObject = JSONObject()
        jsonObject.put("alphabet", finiteAutomaton.alphabet.joinToString(separator = ""))
        jsonObject.put("forceDeterminism", finiteAutomaton.forceDeterminism)
        val states = JSONArray()
        jsonObject.put("states", states)
        val transitions = JSONArray()
        jsonObject.put("transitions", transitions)
        finiteAutomaton.stateNames.forEach { stateName ->
            val state = JSONObject()
            states.put(state)
            state.put("name", stateName)
            finiteAutomaton.isStateAccepting(stateName).let {
                if (it!!) state.put("accepting", it)
            }
            finiteAutomaton.isStateInitial(stateName).let {
                if (it!!) state.put("initial", it)
            }
            finiteAutomaton.getTransitions(stateName)?.forEach { targetStateName ->
                val transition = JSONObject()
                transitions.put(transition)
                transition.put("startState", stateName)
                transition.put("targetState", targetStateName)
                transition.put(
                    "inputCharacters",
                    finiteAutomaton.getInputCharacters(stateName, targetStateName)!!.joinToString(separator = "")
                )
            }
        }
        return jsonObject.toString(if (indent) 4 else 0)
    }

}

/**
 * Serializes this [FiniteAutomaton] to a json string.
 * If [indent] is ```true``` the json string is indented with 4 spaces.
 */
fun FiniteAutomaton.serialize(indent: Boolean = false) = Serializer.serialize(this, indent)
