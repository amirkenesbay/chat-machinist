package kz.rmr.chatmachinist.api.transition

import kz.rmr.chatmachinist.api.DSLBuilder
import kz.rmr.chatmachinist.model.ActionContext

@DSLBuilder
interface TransitionBuilder<STATE : Any, CONTEXT : Any> {
    var startDialog: Boolean
    var name: String?
    fun action(action: Action<STATE, CONTEXT>)
    fun condition(init: ConditionBuilder<STATE, CONTEXT>.() -> Unit): ConditionBuilder<STATE, CONTEXT>
    fun then(init: ThenBuilder<STATE, CONTEXT>.() -> Unit): ThenBuilder<STATE, CONTEXT>
}

typealias Action<STATE, CONTEXT> = ActionContext<STATE, CONTEXT>.() -> Unit
