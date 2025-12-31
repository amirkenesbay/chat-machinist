package kz.rmr.chatmachinist.api.transition

import kz.rmr.chatmachinist.api.DSLBuilder
import kz.rmr.chatmachinist.model.ActionContext
import kz.rmr.chatmachinist.model.EventType

@DSLBuilder
interface ConditionBuilder<STATE : Any, CONTEXT : Any> {
    var eventType: EventType?
    var eventTypes: List<EventType>?
    var from: STATE?
    var button: Enum<*>?
    var text: String?
    var repliedToMessage: Boolean?
    fun guard(guard: Guard<STATE, CONTEXT>)
}

typealias Guard<STATE, CONTEXT> = ActionContext<STATE, CONTEXT>.() -> Boolean
