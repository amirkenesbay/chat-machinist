package kz.rmr.chatmachinist.service

import kz.rmr.chatmachinist.model.EventType
import org.telegram.telegrambots.meta.api.objects.Update

interface EventTypeMatcher {

    fun match(update: Update): EventType
}