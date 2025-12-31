package kz.rmr.chatmachinist.utils

import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update

fun Update.user() = message?.from ?: callbackQuery?.from ?: throw IllegalStateException("No user id")

fun Update.userId() = user().id

fun Update.chatId() =
    message?.chat?.id?.toString()
        ?: (callbackQuery?.message as? Message)?.chat?.id?.toString()
        ?: throw IllegalStateException("No chat id")