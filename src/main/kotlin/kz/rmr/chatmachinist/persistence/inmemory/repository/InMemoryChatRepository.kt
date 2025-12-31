package kz.rmr.chatmachinist.persistence.inmemory.repository

import kz.rmr.chatmachinist.model.Chat
import kz.rmr.chatmachinist.persistence.ChatRepository

class InMemoryChatRepository<STATE, CONTEXT>: ChatRepository<STATE, CONTEXT> {

    private val memory = mutableListOf<Chat<STATE, CONTEXT>>()
    private var idGenerator = 0
    override fun findByExternalId(externalId: String): Chat<STATE, CONTEXT>? {
        return memory.find {
            it.externalId == externalId
        }
    }

    override fun save(chat: Chat<STATE, CONTEXT>): Chat<STATE, CONTEXT> {
        memory.removeIf { it.id == chat.id }
        memory.add(chat)

        if (chat.id != null) {
            return chat
        }

        idGenerator++

        return chat.copy(
            id = idGenerator.toString(),
            dialogs = chat.dialogs.map {
                if (it.id != null) {
                    it
                } else {
                    idGenerator++
                    it.copy(id = idGenerator.toString())
                }
            }.toMutableList()
        )
    }
}