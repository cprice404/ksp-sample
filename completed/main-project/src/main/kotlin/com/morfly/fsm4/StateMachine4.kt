package com.morfly.fsm4

interface Event
interface State<T : Event> {
    fun onEnter(): T
}

sealed class AllTheStates<T : Event> : State<T> {
    object Beginning : AllTheStates<Beginning.Events>() {
        sealed class Events : Event {
            object Complete : Events()
        }

        override fun onEnter(): Events {
            return Events.Complete
        }
    }
    object GetAllExistingInstances : AllTheStates<GetAllExistingInstances.Events>() {
        sealed class Events : Event {
            object NoExistingInstances : Events()
            data class FoundExistingInstances(val existingInstances: List<String>) : Events()
        }

        override fun onEnter(): Events {
            return Events.FoundExistingInstances(listOf("foo", "bar"))
        }
    }
    object CreateInitialInstances : AllTheStates<CreateInitialInstances.Events>() {
        sealed class Events : Event {
            data class Complete(val instances: List<String>) : Events()
        }

        override fun onEnter(): Events {
            return Events.Complete(listOf("newInstance1", "newInstance2"))
        }
    }
    data class ReplaceExistingInstances(val existingInstances: List<String>) :
        AllTheStates<ReplaceExistingInstances.Events>() {
        sealed class Events : Event {
            data class Complete(val instances: List<String>) : Events()
        }

        override fun onEnter(): Events {
            return Events.Complete(listOf("replacedInstance1", "replacedInstance2"))
        }
    }
    data class Done(val allInstances: List<String>) : AllTheStates<Done.Events>() {
        sealed class Events : Event {
            data class Complete(val instances: List<String>) : Events()
        }

        override fun onEnter(): Events {
            return Events.Complete(allInstances)
        }
    }
}

class MyAwesomeStateMachine {
    private var currentState: AllTheStates<*>? = AllTheStates.Beginning

    fun run() {
        while (currentState != null) {
            val state = currentState!!
            println("ENTERING STATE: $state")
            val nextEvent = state.onEnter()
            println("NEXT EVENT: $nextEvent")
            currentState = transition(state, nextEvent)
        }
        println("STATE MACHINE COMPLETE")
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
