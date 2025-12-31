package kz.rmr.chatmachinist.model

import kz.rmr.chatmachinist.api.transition.TriggerChatIdResolver
import kz.rmr.chatmachinist.builders.TriggerChatNameResolver
import kz.rmr.chatmachinist.api.transition.TriggerContextBuilder
import kz.rmr.chatmachinist.api.transition.TriggerDialogIdResolver

data class TriggerDefinition<STATE, CONTEXT>(
    val triggerChatNameResolver: TriggerChatNameResolver<STATE, CONTEXT>,
    val triggerContextBuilder: TriggerContextBuilder<STATE, CONTEXT, *>?,
    val triggerChatIdResolver: TriggerChatIdResolver<STATE, CONTEXT>,
    val triggerDialogIdResolver: TriggerDialogIdResolver<STATE, CONTEXT>?,
)