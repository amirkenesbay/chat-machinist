package kz.rmr.chatmachinist.api.reply

import kz.rmr.chatmachinist.api.DSLBuilder
import kz.rmr.chatmachinist.builders.RepliesBuilderImpl

@DSLBuilder
interface RepliesBuilder<STATE : Enum<STATE>, CONTEXT : Any>  {
    var chatName: String?

    fun reply(init: ReplyBuilder<STATE, CONTEXT>.() -> Unit): ReplyBuilder<STATE, CONTEXT>
}

fun <STATE : Enum<STATE>, CONTEXT : Any> replies(init: RepliesBuilderImpl<STATE, CONTEXT>.() -> Unit): RepliesBuilderImpl<STATE, CONTEXT> {
    val transitionBuilder = RepliesBuilderImpl<STATE, CONTEXT>()
    transitionBuilder.init()
    return transitionBuilder
}