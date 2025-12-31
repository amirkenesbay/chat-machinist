package kz.rmr.chatmachinist.model

import kz.rmr.chatmachinist.api.transition.Guard

data class DesiredConditionDefinition<STATE, CONTEXT>(
    val eventTypes: List<EventType>,
    val from: STATE?,
    val buttonType: Enum<*>?,
    val text: String?,
    val guard: Guard<STATE, CONTEXT>?,
    val repliedToMessage: Boolean?,
)

