package kz.rmr.chatmachinist.service

import kz.rmr.chatmachinist.model.Chat
import kz.rmr.chatmachinist.model.ChatDefinition
import kz.rmr.chatmachinist.model.MatchedTransition
import org.telegram.telegrambots.meta.api.objects.Update

interface TransitionMatcher<STATE: Any, CONTEXT: Any> {

    fun match(
        chatDefinition: ChatDefinition<STATE, CONTEXT>,
        update: Update,
        chat: Chat<STATE, CONTEXT>,
    ): MatchedTransition<STATE, CONTEXT>?
}