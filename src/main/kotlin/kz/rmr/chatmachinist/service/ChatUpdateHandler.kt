package kz.rmr.chatmachinist.service

import kz.rmr.chatmachinist.model.ChatDefinition
import kz.rmr.chatmachinist.model.ChatHandleResult
import kz.rmr.chatmachinist.model.RepliesDefinition
import org.telegram.telegrambots.meta.api.objects.Update

interface ChatUpdateHandler<STATE : Any, CONTEXT : Any> {

    fun handle(
        update: Update,
        chatDefinition: ChatDefinition<STATE, CONTEXT>,
        repliesDefinition: RepliesDefinition<STATE, CONTEXT>
    ): ChatHandleResult<STATE, CONTEXT>
}