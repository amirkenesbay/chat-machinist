package kz.rmr.chatmachinist.api.transition

import kz.rmr.chatmachinist.api.DSLBuilder
import kz.rmr.chatmachinist.model.ActionContext

@DSLBuilder
interface TriggerBuilder<STATE : Any, CONTEXT : Any> {
    var chatName: String?
    var sameDialog: Boolean
    fun <TRIGGER_CONTEXT> triggerContext(triggerContextBuilder: TriggerContextBuilder<STATE, CONTEXT, TRIGGER_CONTEXT>)
    fun chatId(triggerChatIdResolver: TriggerChatIdResolver<STATE, CONTEXT>)
    fun dialogId(triggerDialogIdResolver: TriggerDialogIdResolver<STATE, CONTEXT>)
}

typealias TriggerChatIdResolver<STATE, CONTEXT> = ActionContext<STATE, CONTEXT>.() -> Long
typealias TriggerDialogIdResolver<STATE, CONTEXT> = ActionContext<STATE, CONTEXT>.() -> String
typealias TriggerContextBuilder<STATE, CONTEXT, TRIGGER_CONTEXT> = ActionContext<STATE, CONTEXT>.() -> TRIGGER_CONTEXT
