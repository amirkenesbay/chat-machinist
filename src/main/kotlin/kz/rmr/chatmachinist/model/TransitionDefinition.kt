package kz.rmr.chatmachinist.model

import kz.rmr.chatmachinist.api.transition.Action

data class TransitionDefinition<STATE, CONTEXT>(
    val name: String,
    val startDialog: Boolean,
    val action: Action<STATE, CONTEXT>?,
    val desiredConditions: List<DesiredConditionDefinition<STATE, CONTEXT>>,
    val thenDefinition: ThenDefinition<STATE, CONTEXT>
)