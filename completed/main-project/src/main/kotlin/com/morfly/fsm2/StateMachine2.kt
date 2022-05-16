package com.morfly.fsm2

sealed class AllTheStates<T : Event> {
    object Beginning : AllTheStates<BeginningEvents>()
    object GetAllExistingInstances : AllTheStates<GetAllExistingInstancesEvents>()
    object CreateInitialInstances : AllTheStates<CreateInitialInstancesEvents>()
    data class ReplaceExistingInstances(val existingInstances: List<String>) : AllTheStates<ReplaceExistingInstancesEvents>()
    data class Done(val allInstances: List<String>) : AllTheStates<DoneEvents>()
}

interface Event

sealed class BeginningEvents : Event {
    object Complete : BeginningEvents()
}

sealed class GetAllExistingInstancesEvents : Event {
    object NoExistingInstances : GetAllExistingInstancesEvents()
    data class FoundExistingInstances(val existingInstances: List<String>) : GetAllExistingInstancesEvents()
}

sealed class CreateInitialInstancesEvents : Event {
    data class Complete(val instances: List<String>) : CreateInitialInstancesEvents()
}

sealed class ReplaceExistingInstancesEvents : Event {
    data class Complete(val instances: List<String>) : ReplaceExistingInstancesEvents()
}

sealed class DoneEvents : Event {
    data class Complete(val instances: List<String>) : DoneEvents()
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
            AllTheStates.Beginning -> BeginningEvents.Complete
            AllTheStates.GetAllExistingInstances -> {
                GetAllExistingInstancesEvents.FoundExistingInstances(listOf("foo", "bar"))
            }
            AllTheStates.CreateInitialInstances -> CreateInitialInstancesEvents.Complete(listOf("newInstance1", "newInstance2"))
            is AllTheStates.ReplaceExistingInstances -> ReplaceExistingInstancesEvents.Complete(listOf("replacedInstance1", "replacedInstance2"))
            is AllTheStates.Done -> DoneEvents.Complete(state.allInstances)
        }
    }

    private fun transition(state: AllTheStates<*>, event: Event): AllTheStates<*>? {
        return when (state) {
            AllTheStates.Beginning -> when (val e = event as BeginningEvents) {
                BeginningEvents.Complete -> AllTheStates.GetAllExistingInstances
            }
            AllTheStates.GetAllExistingInstances -> {
                when (val e = event as GetAllExistingInstancesEvents) {
                    is GetAllExistingInstancesEvents.FoundExistingInstances -> AllTheStates.ReplaceExistingInstances(
                        e.existingInstances
                    )
                    GetAllExistingInstancesEvents.NoExistingInstances -> AllTheStates.CreateInitialInstances
                }
            }
            AllTheStates.CreateInitialInstances -> when (val e = event as CreateInitialInstancesEvents) {
                is CreateInitialInstancesEvents.Complete -> AllTheStates.Done(e.instances)
            }
            is AllTheStates.ReplaceExistingInstances -> when (val e = event as ReplaceExistingInstancesEvents) {
                is ReplaceExistingInstancesEvents.Complete -> AllTheStates.Done(e.instances)
            }
            is AllTheStates.Done -> when (val e = event as DoneEvents) {
                is DoneEvents.Complete -> null
            }
        }
    }
}

fun main() {
    val machine = MyAwesomeStateMachine()
    machine.run()
}
