package kz.rmr.chatmachinist.model


data class ThenDefinition<STATE, CONTEXT>(
    val to: STATE & Any,
    val triggerDefinition: TriggerDefinition<STATE, CONTEXT>?,
    val noReply: Boolean
)