package kz.rmr.chatmachinist.api.reply

import kz.rmr.chatmachinist.api.DSLBuilder

@DSLBuilder
interface ReplyBuilder<STATE : Enum<STATE>, CONTEXT : Any> {
    var state: STATE?

    fun message(init: MessageBuilder<STATE, CONTEXT>.() -> Unit)
}

enum class ParseMode {
    DEFAULT,
    HTML
}
