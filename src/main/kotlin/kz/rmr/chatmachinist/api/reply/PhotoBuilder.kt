package kz.rmr.chatmachinist.api.reply

import kz.rmr.chatmachinist.api.DSLBuilder

@DSLBuilder
interface PhotoBuilder<STATE : Enum<STATE>, CONTEXT : Any> {
    var fileId: String?
}