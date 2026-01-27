package kz.rmr.chatmachinist.service.impl

import kz.rmr.chatmachinist.model.ButtonData
import kz.rmr.chatmachinist.model.EventType
import kz.rmr.chatmachinist.model.TriggerData
import kz.rmr.chatmachinist.service.CallbackDataService
import kz.rmr.chatmachinist.service.EventTypeMatcher
import org.telegram.telegrambots.meta.api.objects.Update

class EventTypeMatcherImpl(
    private val callbackDataService: CallbackDataService
) : EventTypeMatcher {

    override fun match(update: Update): EventType {

        if (update.callbackQuery != null) {
            return when (callbackDataService.decode(update.callbackQuery.data)) {
                is ButtonData -> EventType.INLINE_BUTTON_CLICKED
                is TriggerData -> EventType.TRIGGERED
                else -> throw IllegalStateException("")
            }
        }

        if (update.message.text != null) {
            if (update.message.text.startsWith("/")) {
                return EventType.COMMAND
            }
            return EventType.TEXT
        }

        if (update.message.document != null) {
            return EventType.DOCUMENT
        }

        if (update.message.photo != null) {
            return EventType.PHOTO
        }

        if (update.message.voice != null) {
            return EventType.VOICE
        }

        throw IllegalStateException("Cannot match event type")
    }
}