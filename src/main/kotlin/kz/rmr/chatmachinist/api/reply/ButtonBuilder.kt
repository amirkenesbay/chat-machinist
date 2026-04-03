package kz.rmr.chatmachinist.api.reply

import kz.rmr.chatmachinist.api.DSLBuilder

@DSLBuilder
interface ButtonBuilder<STATE : Enum<STATE>, CONTEXT : Any> {
    var text: String?
    var textCode: String?
    var type: Enum<*>?
    var link: String?
    var data: String?
}