package kz.rmr.chatmachinist.api.transition

import kz.rmr.chatmachinist.api.DSLBuilder

@DSLBuilder
interface ThenBuilder<STATE : Any, CONTEXT : Any> {
    var to: STATE?
    var noReply: Boolean
    fun trigger(init: TriggerBuilder<STATE, CONTEXT>.() -> Unit): TriggerBuilder<STATE, CONTEXT>
}