package kz.rmr.chatmachinist.service.impl

import kz.rmr.chatmachinist.model.Chat
import kz.rmr.chatmachinist.model.ChatDefinition
import kz.rmr.chatmachinist.persistence.ChatRepository
import kz.rmr.chatmachinist.service.ChatService
import kz.rmr.chatmachinist.utils.chatId
import kz.rmr.chatmachinist.utils.user
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User

@Service
class ChatServiceImpl<STATE : Any, CONTEXT : Any>(
    private val chatRepository: ChatRepository<STATE, CONTEXT>
) : ChatService<STATE, CONTEXT> {

    override fun getOrCreate(update: Update, chatDefinition: ChatDefinition<STATE, CONTEXT>): Chat<STATE, CONTEXT> {
        val externalId = update.chatId()
        val user = update.user()
        return chatRepository.findByExternalId(externalId) ?: defaultChat(chatDefinition, externalId, user)
    }

    private fun defaultChat(
        chatDefinition: ChatDefinition<STATE, CONTEXT>,
        externalId: String,
        user: User
    ): Chat<STATE, CONTEXT> = Chat(
        id = null,
        name = chatDefinition.name,
        externalId = externalId,
        user = user,
        dialogs = mutableListOf(),
        languageCode = user.languageCode
    )

    override fun save(chat: Chat<STATE, CONTEXT>) = chatRepository.save(chat)

}