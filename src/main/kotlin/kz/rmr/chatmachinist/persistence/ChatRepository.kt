package kz.rmr.chatmachinist.persistence

import kz.rmr.chatmachinist.model.Chat

interface ChatRepository<STATE, CONTEXT>: Repository<Chat<STATE, CONTEXT>> {

    fun findByExternalId(externalId: String): Chat<STATE, CONTEXT>?

    override fun save(it: Chat<STATE, CONTEXT>): Chat<STATE, CONTEXT>
}