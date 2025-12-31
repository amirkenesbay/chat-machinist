package kz.rmr.chatmachinist.service

import kz.rmr.chatmachinist.model.Chat
import kz.rmr.chatmachinist.model.MatchedTransition
import kz.rmr.chatmachinist.model.RepliesDefinition
import kz.rmr.chatmachinist.model.ReplyResult
import org.telegram.telegrambots.meta.api.objects.Update

interface ReplyHandler<STATE : Any, CONTEXT : Any> {

    fun handle(
        update: Update,
        chat: Chat<STATE, CONTEXT>,
        repliesDefinition: RepliesDefinition<STATE, CONTEXT>,
        matched: MatchedTransition<STATE, CONTEXT>
    ): ReplyResult
}