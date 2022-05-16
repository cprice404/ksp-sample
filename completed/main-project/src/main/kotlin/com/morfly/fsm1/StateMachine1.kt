package com.morfly.fsm1

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
            is AllTheStates.Done -> DoneEvents.Complete((state as AllTheStates.Done).allInstances)
        }
    }

    private fun transition(state: AllTheStates<*>, e: Event): AllTheStates<*>? {
        return when (state) {
            AllTheStates.Beginning -> when (e as BeginningEvents) {
                BeginningEvents.Complete -> AllTheStates.GetAllExistingInstances
            }
            AllTheStates.GetAllExistingInstances -> when (e as GetAllExistingInstancesEvents) {
                is GetAllExistingInstancesEvents.FoundExistingInstances -> AllTheStates.ReplaceExistingInstances((e as GetAllExistingInstancesEvents.FoundExistingInstances).existingInstances)
                GetAllExistingInstancesEvents.NoExistingInstances -> AllTheStates.CreateInitialInstances
            }
            AllTheStates.CreateInitialInstances -> when (e as CreateInitialInstancesEvents) {
                is CreateInitialInstancesEvents.Complete -> AllTheStates.Done((e as CreateInitialInstancesEvents.Complete).instances)
            }
            is AllTheStates.ReplaceExistingInstances -> when (e as ReplaceExistingInstancesEvents) {
                is ReplaceExistingInstancesEvents.Complete -> AllTheStates.Done((e as ReplaceExistingInstancesEvents.Complete).instances)
            }
            is AllTheStates.Done -> when (e as DoneEvents) {
                is DoneEvents.Complete -> null
            }
        }
    }
}

fun main() {
    val machine = MyAwesomeStateMachine()
    machine.run()
}
