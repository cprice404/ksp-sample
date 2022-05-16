package com.morfly.fsm7

interface State<TStates> {
    fun onEnter(): TStates?
}
sealed class AllTheStates : State<AllTheStates> {
    object Beginning : AllTheStates() {
        override fun onEnter(): AllTheStates {
            return GetAllExistingInstances
        }
    }

    object GetAllExistingInstances : AllTheStates() {
        override fun onEnter(): AllTheStates {
            val existingInstances = listOf("foo", "bar")
            return if (existingInstances.isEmpty()) {
                CreateInitialInstances
            } else {
                ReplaceExistingInstances(existingInstances)
            }
        }
    }

    object CreateInitialInstances : AllTheStates() {
        override fun onEnter(): AllTheStates {
            val newInstances = listOf("newInstance1", "newInstance2")
            return Done(newInstances)
        }
    }

    data class ReplaceExistingInstances(val existingInstances: List<String>) : AllTheStates() {
        override fun onEnter(): AllTheStates {
            return Done(listOf("replacedInstance1", "replacedInstance2"))
        }
    }

    data class Done(val allInstances: List<String>) : AllTheStates() {
        override fun onEnter(): AllTheStates? {
            return null
        }
    }
}

class StateMachine<TState : State<TState>>(
    initialState: State<TState>
) {
    private var currentState: State<TState>? = initialState

    fun run() {
        println("STATE MACHINE BEGINNING")
        while (currentState != null) {
            println("CURRENT STATE: $currentState")
            currentState = currentState!!.onEnter()
        }
        println("STATE MACHINE COMPLETE")
    }
}

fun main() {
    val machine = StateMachine(AllTheStates.Beginning)
    machine.run()
}
