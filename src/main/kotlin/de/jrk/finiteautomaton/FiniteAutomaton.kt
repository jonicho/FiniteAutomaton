package de.jrk.finiteautomaton

/**
 * A finite automaton with the given [alphabet] which can be forced to be deterministic (throw an exception if
 * something is added that would make the automaton non-deterministic) if [forceDeterminism] is ```true```.
 */
class FiniteAutomaton(alphabet: List<Char>, val forceDeterminism: Boolean = false) {
    private val states: MutableSet<State> = mutableSetOf()

    /**
     * This automaton's alphabet
     */
    val alphabet: List<Char> = alphabet.toList()

    /**
     * The names of all states.
     */
    val stateNames get() = states.map { it.name }

    /**
     * The number of states in this automaton.
     */
    val size get() = states.size

    /**
     * A copy of this automaton.
     */
    val copy
        get() = FiniteAutomaton(
            alphabet,
            forceDeterminism
        ).also {
            it.states.addAll(states.map { state ->
                State(
                    state.name,
                    state.accepting,
                    state.initial,
                    state.transitions.toMutableSet()
                )
            })
        }

    /**
     * Returns whether the state with the given [stateName] is an initial state.
     * Returns ```null``` if there is no such state.
     */
    fun isStateInitial(stateName: String) = states[stateName]?.initial

    /**
     * Returns whether the state with the given [stateName] is accepting.
     * Returns ```null``` if there is no such state.
     */
    fun isStateAccepting(stateName: String) = states[stateName]?.accepting

    /**
     * Returns the names of the target states of the transitions going from the state with the given [startStateName].
     * Returns ```null``` if there is no state with the given [startStateName].
     */
    fun getTransitions(startStateName: String) = states[startStateName]?.transitions?.map { it.targetState.name }

    /**
     * Returns the input characters of the transition from the state with the given [startStateName] to
     * the state with the given [targetStateName].
     * Returns ```null``` if there is no such transition.
     */
    fun getInputCharacters(startStateName: String, targetStateName: String) =
        states[startStateName]?.transitions?.find { it.targetState == states[targetStateName] ?: return null }?.inputCharacters

    /**
     * Adds a state with the given [stateName] which can be [accepting] and/or [initial].
     */
    fun addState(stateName: String, accepting: Boolean = false, initial: Boolean = false) {
        if (forceDeterminism && initial) {
            require(states.none { it.initial }) { "There can only be one initial state in a deterministic automaton!" }
        }
        val added = states.add(State(stateName, accepting, initial))
        if (!added) {
            throw IllegalArgumentException("State $stateName already exists!")
        }
    }

    /**
     * Adds a transition going from the state with the given [startStateName]
     * to the state with the given [targetStateName] with the given [inputCharacters].
     */
    fun addTransition(startStateName: String, targetStateName: String, inputCharacters: List<Char>) {
        val startState = requireNotNull(states[startStateName]) { "State $startStateName does not exist!" }
        val targetState = requireNotNull(states[targetStateName]) { "State $targetStateName does not exist!" }
        require(startState.transitions.none { it.targetState == targetState }) { "There is already a transition from state $startStateName to state $targetStateName!" }
        require(alphabet.containsAll(inputCharacters)) { "The input characters have to a subset of the alphabet!" }
        if (forceDeterminism) {
            require(startState.transitions.none { transition ->
                transition.inputCharacters.any { it in inputCharacters }
            }) { "In an deterministic automaton a input character can only be in one transition of a state." }
        }
        startState.transitions.add(Transition(targetState, inputCharacters.toList()))
    }

    /**
     * Returns whether this automaton is deterministic.
     */
    fun isDeterministic() =
        states.count { it.initial } <= 1
                && states.none { state -> state.transitions.any { transition -> transition.inputCharacters.isEmpty() } }
                && states.none { state ->
            state.transitions.any { transition ->
                transition.inputCharacters.any { char ->
                    state.transitions.minus(transition).any { char in it.inputCharacters }
                }
            }
        }

    /**
     * Checks whether this automaton accepts the given [string].
     */
    fun checkString(string: String): Boolean {
        if (!isDeterministic()) {
            TODO("Not implemented for non-deterministic automatons!")
        }
        var currentState =
            states.find { it.initial } ?: throw IllegalStateException("This automaton does not have an initial state!")
        string.forEach { char ->
            currentState = currentState.transitions.find { char in it.inputCharacters }?.targetState ?: return false
        }
        return currentState.accepting
    }


    override fun toString(): String {
        return "FiniteAutomaton(forceDeterminism=$forceDeterminism, alphabet=$alphabet, states=$states)"
    }

    private data class State(
        val name: String,
        val accepting: Boolean = false,
        val initial: Boolean = false,
        val transitions: MutableSet<Transition> = mutableSetOf()
    ) {
        override fun equals(other: Any?) = other is State && other.name == name
        override fun hashCode() = name.hashCode()
    }

    private data class Transition(val targetState: State, val inputCharacters: List<Char>) {
        override fun equals(other: Any?) = other is Transition && other.targetState == targetState
        override fun hashCode() = targetState.hashCode()
        override fun toString() = "Transition(targetState=${targetState.name}, inputCharacters=$inputCharacters)"
    }

    private operator fun MutableSet<State>.get(name: String): State? = find { it.name == name }
}
