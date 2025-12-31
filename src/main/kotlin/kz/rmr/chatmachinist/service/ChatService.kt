package kz.rmr.chatmachinist.service

import kz.rmr.chatmachinist.model.Chat
import kz.rmr.chatmachinist.model.ChatDefinition
import org.telegram.telegrambots.meta.api.objects.Update

interface ChatService<STATE: Any, CONTEXT: Any> {

    fun getOrCreate(update: Update, chatDefinition: ChatDefinition<STATE, CONTEXT>): Chat<STATE, CONTEXT>

    fun save(chat: Chat<STATE, CONTEXT>): Chat<STATE, CONTEXT>
}