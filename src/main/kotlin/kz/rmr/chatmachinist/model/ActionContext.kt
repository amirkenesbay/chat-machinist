package kz.rmr.chatmachinist.model

import org.telegram.telegrambots.meta.api.objects.*
import org.telegram.telegrambots.meta.api.objects.Chat

data class ActionContext<STATE, CONTEXT>(
    val update: Update,
    val text: String?,
    val context: CONTEXT,
    val from: STATE?,
    val to: STATE?,
    val eventType: EventType,
    val user: User,
    val chat: Chat,
    val chatName: String,
    val dialogId: String?,
    val buttonTypeName: String?,
    val buttonText: String?,
    val _triggerContext: Any?,
    val repliedToMessageId: Int?,
    var languageCode: String?,
    var photos: List<PhotoSize>?,
    var document: Document?,
    var voice: Voice?
) {
    inline fun <reified TRIGGER_CONTEXT> triggerContext(): TRIGGER_CONTEXT? {
        return _triggerContext as? TRIGGER_CONTEXT
    }
}