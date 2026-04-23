package kz.rmr.chatmachinist.service.impl

import kz.rmr.chatmachinist.model.ActionContext
import kz.rmr.chatmachinist.model.TransitionDefinition
import kz.rmr.chatmachinist.model.TriggerData
import kz.rmr.chatmachinist.service.CallbackDataService
import kz.rmr.chatmachinist.service.TriggerHandler
import mu.KotlinLogging
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import java.util.*
import kotlin.reflect.jvm.jvmName

class TriggerHandlerImpl<STATE : Any, CONTEXT : Any>(
    private val callbackDataService: CallbackDataService
) : TriggerHandler<STATE, CONTEXT> {

    private val logger = KotlinLogging.logger(this::class.jvmName)
    private val random = Random()

    override fun handle(
        transitionDefinition: TransitionDefinition<STATE, CONTEXT>,
        actionContext: ActionContext<STATE, CONTEXT>
    ): Update {
        logger.debug { "Starting trigger" }
        val triggerDefinition = transitionDefinition.thenDefinition.triggerDefinition!!

        val triggerChatName =
            triggerDefinition.triggerChatNameResolver(actionContext)
        val triggerContext: Any? =
            triggerDefinition.triggerContextBuilder?.invoke(actionContext)
        val triggerChatId =
            triggerDefinition.triggerChatIdResolver(actionContext)
        val triggerDialogId =
            triggerDefinition.triggerDialogIdResolver?.invoke(actionContext)

        val triggerData = TriggerData(
            triggerChatName,
            triggerChatId,
            triggerContext,
            triggerDialogId
        )

        val originalMessageId = actionContext.update.callbackQuery?.message?.messageId
            ?: actionContext.update.message?.messageId
        val sameChat = triggerChatId == actionContext.chat.id

        val triggerUpdate = Update().apply {
            updateId = random.nextInt()

            callbackQuery = CallbackQuery().apply {
                this.data = callbackDataService.encode(
                    triggerData,
                )
                this.from = actionContext.user
                this.message = Message().apply {
                    this.chat = org.telegram.telegrambots.meta.api.objects.Chat().apply {
                        this.id = triggerChatId
                    }
                    if (sameChat && originalMessageId != null) {
                        this.messageId = originalMessageId
                    }
                }
            }
        }
        return triggerUpdate
    }
}