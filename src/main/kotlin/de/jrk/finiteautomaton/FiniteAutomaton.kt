package de.jrk.finiteautomaton

/**
 * A finite automaton with the given [alphabet] which can be forced to be deterministic (throw an exception if
 * something is added that would make the automaton non-deterministic) if [forceDeterminism] is ```true```.
 */
class FiniteAutomaton(alphabet: List<Char>, val forceDeterminism: Boolean = false) {
    private val states: MutableList<State> = mutableListOf()

    /**
     * This automaton's alphabet
     */
    val alphabet: List<Char> = alphabet.toList()

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
                    state.accepting,
                    state.initial,
                    state.transitions.toMutableList()
                )
            })
        }

    /**
     * Returns whether the given [state] is an initial state.
     */
    fun isStateInitial(state: Int) = states[state].initial

    /**
     * Returns whether the given [state] is accepting.
     */
    fun isStateAccepting(state: Int) = states[state].accepting

    /**
     * Returns the indices of the target states of the transitions going from the given [startState].
     */
    fun getTransitions(startState: Int) = states[startState].transitions.map { it.targetState }

    /**
     * Returns the input characters of the transition from the given [startState] to the given [targetState].
     * Returns ```null``` if there is no such transition.
     */
    fun getInputCharacters(startState: Int, targetState: Int) =
        states[startState].transitions.find { it.targetState == targetState }?.inputCharacters

    /**
     * Adds a state which can be [accepting] and/or [initial].
     */
    fun addState(accepting: Boolean = false, initial: Boolean = false) {
        if (forceDeterminism && states.any { it.initial }) {
            throw IllegalStateException("There can only be one initial state in a deterministic automaton!")
        }
        states.add(State(initial, accepting))
    }

    /**
     * Adds a transition going from the given [startState] to the given [targetState] with the given [inputCharacters].
     */
    fun addTransition(startState: Int, targetState: Int, inputCharacters: List<Char>) {
        if (states[startState].transitions.any { it.targetState == targetState }) {
            throw IllegalArgumentException("There is already a transition from state $startState to state $targetState!")
        }
        if (!alphabet.containsAll(inputCharacters)) {
            throw IllegalArgumentException("The input characters have to a subset of the alphabet!")
        }
        if (forceDeterminism && states[startState].transitions.any { transition ->
                transition.inputCharacters.any { it in inputCharacters }
            }) {
            throw IllegalArgumentException("In an deterministic automaton a input character can only be in one transition of a state.")
        }
        states[startState].transitions.add(Transition(targetState, inputCharacters.toList()))
    }

    /**
     * Returns whether this automaton is deterministic.
     */
    fun isDeterministic() =
        states.count { it.initial } <= 1
                && !states.any { state -> state.transitions.any { transition -> transition.inputCharacters.isEmpty() } }
                && !states.any { state ->
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
        var currentState = states.indexOfFirst { it.initial }
        if (currentState == -1) {
            throw IllegalStateException("This automaton does not have an initial state!")
        }
        string.forEach { char ->
            currentState =
                states[currentState].transitions.find { char in it.inputCharacters }?.targetState ?: return false
        }
        return states[currentState].accepting
    }

    private data class State(
        val accepting: Boolean = false,
        val initial: Boolean = false,
        val transitions: MutableList<Transition> = mutableListOf()
    )

    private data class Transition(val targetState: Int, val inputCharacters: List<Char>)
}
