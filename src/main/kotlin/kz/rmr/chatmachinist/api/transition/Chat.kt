package kz.rmr.chatmachinist.api.transition

import kz.rmr.chatmachinist.api.DSLBuilder
import kz.rmr.chatmachinist.builders.ChatBuilderImpl

@DSLBuilder
interface ChatBuilder<STATE : Any, CONTEXT : Any> {
    var name: String?
    fun dialog(init: DialogBuilder<STATE, CONTEXT>.() -> Unit): DialogBuilder<STATE, CONTEXT>
    fun initialContext(contextInitializer: ContextInitializer<CONTEXT>)
    fun commands(init: CommandsBuilder<STATE, CONTEXT>.() -> Unit): CommandsBuilder<STATE, CONTEXT>
}

typealias ContextInitializer<CONTEXT> = () -> CONTEXT

fun <STATE : Any, CONTEXT : Any> chat(init: ChatBuilder<STATE, CONTEXT>.() -> Unit): ChatBuilder<STATE, CONTEXT> {
    val transitionBuilder = ChatBuilderImpl<STATE, CONTEXT>()
    transitionBuilder.init()
    return transitionBuilder
}
