package kz.rmr.chatmachinist.api.transition

import kz.rmr.chatmachinist.api.DSLBuilder

@DSLBuilder
interface DialogBuilder<STATE : Any, CONTEXT : Any> {
    var name: String?
    fun transition(init: TransitionBuilder<STATE, CONTEXT>.() -> Unit): TransitionBuilder<STATE, CONTEXT>
}