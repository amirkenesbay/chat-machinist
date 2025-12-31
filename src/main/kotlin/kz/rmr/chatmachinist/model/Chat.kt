package kz.rmr.chatmachinist.model

import org.telegram.telegrambots.meta.api.objects.User

data class Chat<STATE, CONTEXT>(
    val id: String?,
    val name: String,
    val externalId: String,
    val user: User,
    val dialogs: MutableList<Dialog<STATE, CONTEXT>>,
    var languageCode: String?
)