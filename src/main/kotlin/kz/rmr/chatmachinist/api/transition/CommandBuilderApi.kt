package kz.rmr.chatmachinist.api.transition

import kz.rmr.chatmachinist.api.DSLBuilder
import kz.rmr.chatmachinist.model.BotCommandScope

@DSLBuilder
interface CommandBuilderApi<STATE : Any, CONTEXT : Any> {
    var text: String?
    var description: String?
    var scope: BotCommandScope?
}