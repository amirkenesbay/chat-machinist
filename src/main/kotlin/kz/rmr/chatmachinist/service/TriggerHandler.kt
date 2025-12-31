package kz.rmr.chatmachinist.service

import kz.rmr.chatmachinist.model.ActionContext
import kz.rmr.chatmachinist.model.MatchedTransition
import kz.rmr.chatmachinist.model.TransitionDefinition
import org.telegram.telegrambots.meta.api.objects.Update

interface TriggerHandler<STATE : Any, CONTEXT : Any> {


    fun handle(
        transitionDefinition: TransitionDefinition<STATE, CONTEXT>,
        actionContext: ActionContext<STATE, CONTEXT>
    ): Update
}