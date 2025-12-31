package kz.rmr.chatmachinist.api.transition

import kz.rmr.chatmachinist.api.DSLBuilder
import kz.rmr.chatmachinist.builders.CommandBuilderImpl

@DSLBuilder
interface CommandsBuilder<STATE : Any, CONTEXT : Any> {
    fun command(init: CommandBuilderImpl<STATE, CONTEXT>.() -> Unit): CommandBuilderImpl<STATE, CONTEXT>
}