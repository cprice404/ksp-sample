package com.morfly.fsm6

interface Event
interface State<TStates, TEvent : Event> {
    fun onEnter(): TEvent
    fun transition(event: TEvent): TStates?
}

sealed class AllTheStates<T : Event> : State<AllTheStates<*>, T> {
    object Beginning : AllTheStates<Beginning.Events>() {
        sealed class Events : Event {
            object Complete : Events()
        }
        override fun onEnter(): Events {
            return Events.Complete
        }
        override fun transition(event: Events): AllTheStates<*> {
            return when (event) {
                Events.Complete -> GetAllExistingInstances
            }
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
        override fun transition(event: Events): AllTheStates<*> {
            return when (event) {
                is Events.FoundExistingInstances -> ReplaceExistingInstances(event.existingInstances)
                Events.NoExistingInstances -> CreateInitialInstances
            }
        }
    }

    object CreateInitialInstances : AllTheStates<CreateInitialInstances.Events>() {
        sealed class Events : Event {
            data class Complete(val instances: List<String>) : Events()
        }
        override fun onEnter(): Events {
            return Events.Complete(listOf("newInstance1", "newInstance2"))
        }
        override fun transition(event: Events): AllTheStates<*> {
            return when (event) {
                is Events.Complete -> Done(event.instances)
            }
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
        override fun transition(event: Events): AllTheStates<*>? {
            return when (event) {
                is Events.Complete -> Done(event.instances)
            }
        }
    }

    data class Done(val allInstances: List<String>) : AllTheStates<Done.Events>() {
        sealed class Events : Event {
            data class Complete(val instances: List<String>) : Events()
        }
        override fun onEnter(): Events {
            return Events.Complete(allInstances)
        }
        override fun transition(event: Events): AllTheStates<*>? {
            return when (event) {
                is Events.Complete -> null
            }
        }
    }
}

class StateMachine<TState : State<TState, *>>(
    initialState: State<TState, *>
) {
    private var currentState: State<TState, *>? = initialState

    fun run() {
        while (currentState != null) {
            currentState = transition(currentState!!)
        }
        println("STATE MACHINE COMPLETE")
    }

    private fun <TEvent : Event> transition(state: State<TState, TEvent>): State<TState, *>? {
        println("ENTERING STATE: $state")
        val nextEvent = state.onEnter()
        println("NEXT EVENT: $nextEvent")
        return state.transition(nextEvent)
    }
}

fun main() {
    val initialState: AllTheStates<AllTheStates.Beginning.Events> = AllTheStates.Beginning
    val machine = StateMachine(initialState)
    machine.run()
}
