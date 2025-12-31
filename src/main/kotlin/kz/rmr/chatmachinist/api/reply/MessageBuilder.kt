package kz.rmr.chatmachinist.api.reply

import kz.rmr.chatmachinist.api.DSLBuilder
import kz.rmr.chatmachinist.model.ActionContext

@DSLBuilder
interface MessageBuilder<STATE : Enum<STATE>, CONTEXT : Any> {
    val context: CONTEXT
    val actionContext: ActionContext<STATE, CONTEXT>
    var text: String?
    var textCode: String?

    var newMessage: Boolean
    var newPinnedMessage: Boolean
    var parseMode: ParseMode
    var disableLinkPreview: Boolean?

    fun keyboard(init: KeyboardBuilder<STATE, CONTEXT>.() -> Unit): KeyboardBuilder<STATE, CONTEXT>
    fun document(init: DocumentBuilder<STATE, CONTEXT>.() -> Unit): DocumentBuilder<STATE, CONTEXT>
    fun photo(init: PhotoBuilder<STATE, CONTEXT>.() -> Unit): PhotoBuilder<STATE, CONTEXT>
}