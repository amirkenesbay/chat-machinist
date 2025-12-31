package kz.rmr.chatmachinist.api.reply

import kz.rmr.chatmachinist.api.DSLBuilder

@DSLBuilder
interface ButtonRowBuilder<STATE : Enum<STATE>, CONTEXT : Any> {

    fun button(init: ButtonBuilder<STATE, CONTEXT>.() -> Unit): ButtonBuilder<STATE, CONTEXT>
}