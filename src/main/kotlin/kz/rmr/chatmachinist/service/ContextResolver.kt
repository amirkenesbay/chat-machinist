package kz.rmr.chatmachinist.service

import kz.rmr.chatmachinist.model.*
import org.telegram.telegrambots.meta.api.objects.Update

interface ContextResolver<STATE: Any, CONTEXT: Any> {

    fun resolve(
        chatDefinition: ChatDefinition<STATE, CONTEXT>,
        update: Update,
        chat: Chat<STATE, CONTEXT>?,
        dialog: Dialog<STATE, CONTEXT>? = null,
        transitionDefinition: TransitionDefinition<STATE, CONTEXT>? = null
    ): ActionContext<STATE, CONTEXT>
}