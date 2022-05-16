package com.morfly.fsm3

interface Event

sealed class AllTheStates<T : Event> {
    object Beginning : AllTheStates<Beginning.Events>() {
        sealed class Events : Event {
            object Complete : Events()
        }
    }
    object GetAllExistingInstances : AllTheStates<GetAllExistingInstances.Events>() {
        sealed class Events : Event {
            object NoExistingInstances : Events()
            data class FoundExistingInstances(val existingInstances: List<String>) : Events()
        }
    }
    object CreateInitialInstances : AllTheStates<CreateInitialInstances.Events>() {
        sealed class Events : Event {
            data class Complete(val instances: List<String>) : Events()
        }
    }
    data class ReplaceExistingInstances(val existingInstances: List<String>) :
        AllTheStates<ReplaceExistingInstances.Events>() {
        sealed class Events : Event {
            data class Complete(val instances: List<String>) : Events()
        }
    }
    data class Done(val allInstances: List<String>) : AllTheStates<Done.Events>() {
        sealed class Events : Event {
            data class Complete(val instances: List<String>) : Events()
        }
    }
}

class MyAwesomeStateMachine {
    private var currentState: AllTheStates<*>? = AllTheStates.Beginning

    fun run() {
        while (currentState != null) {
            val state = currentState!!
            println("ENTERING STATE: $state")
            val nextEvent = enter(state)
            println("NEXT EVENT: $nextEvent")
            currentState = transition(state, nextEvent)
        }
        println("STATE MACHINE COMPLETE")
    }

    private fun enter(state: AllTheStates<*>): Event {
        return when (state) {
            AllTheStates.Beginning ->
                AllTheStates.Beginning.Events.Complete
            AllTheStates.GetAllExistingInstances ->
                AllTheStates.GetAllExistingInstances.Events.FoundExistingInstances(listOf("foo", "bar"))
            AllTheStates.CreateInitialInstances ->
                AllTheStates.CreateInitialInstances.Events.Complete(listOf("newInstance1", "newInstance2"))
            is AllTheStates.ReplaceExistingInstances ->
                AllTheStates.ReplaceExistingInstances.Events.Complete(listOf("replacedInstance1", "replacedInstance2"))
            is AllTheStates.Done ->
                AllTheStates.Done.Events.Complete(state.allInstances)
        }
    }

    private fun transition(state: AllTheStates<*>, event: Event): AllTheStates<*>? {
        return when (state) {
            AllTheStates.Beginning -> when (val e = event as AllTheStates.Beginning.Events) {
                AllTheStates.Beginning.Events.Complete -> AllTheStates.GetAllExistingInstances
            }
            AllTheStates.GetAllExistingInstances -> {
                when (val e = event as AllTheStates.GetAllExistingInstances.Events) {
                    is AllTheStates.GetAllExistingInstances.Events.FoundExistingInstances -> AllTheStates.ReplaceExistingInstances(
                        e.existingInstances
                    )
                    AllTheStates.GetAllExistingInstances.Events.NoExistingInstances -> AllTheStates.CreateInitialInstances
                }
            }
            AllTheStates.CreateInitialInstances -> when (val e = event as AllTheStates.CreateInitialInstances.Events) {
                is AllTheStates.CreateInitialInstances.Events.Complete -> AllTheStates.Done(e.instances)
            }
            is AllTheStates.ReplaceExistingInstances -> when (val e = event as AllTheStates.ReplaceExistingInstances.Events) {
                is AllTheStates.ReplaceExistingInstances.Events.Complete -> AllTheStates.Done(e.instances)
            }
            is AllTheStates.Done -> when (val e = event as AllTheStates.Done.Events) {
                is AllTheStates.Done.Events.Complete -> null
            }
        }
    }
}

fun main() {
    val machine = MyAwesomeStateMachine()
    machine.run()
}
