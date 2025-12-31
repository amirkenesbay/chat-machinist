package kz.rmr.chatmachinist.api.reply

import kz.rmr.chatmachinist.api.DSLBuilder

@DSLBuilder
interface KeyboardBuilder<STATE : Enum<STATE>, CONTEXT : Any> {
    var inline: Boolean

    fun buttonRow(init: ButtonRowBuilder<*, *>.() -> Unit): ButtonRowBuilder<*, *>
}