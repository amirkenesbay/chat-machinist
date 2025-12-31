package kz.rmr.chatmachinist.service.impl

import kz.rmr.chatmachinist.model.ActionContext
import kz.rmr.chatmachinist.model.ButtonData
import kz.rmr.chatmachinist.model.Chat
import kz.rmr.chatmachinist.model.ChatDefinition
import kz.rmr.chatmachinist.model.Dialog
import kz.rmr.chatmachinist.model.TransitionDefinition
import kz.rmr.chatmachinist.model.TriggerData
import kz.rmr.chatmachinist.service.CallbackDataService
import kz.rmr.chatmachinist.service.ContextResolver
import kz.rmr.chatmachinist.service.EventTypeMatcher
import org.telegram.telegrambots.meta.api.objects.Update

class ContextResolverImpl<STATE: Any, CONTEXT: Any>(
    private val callbackDataService: CallbackDataService,
    private val eventTypeMatcher: EventTypeMatcher
) : ContextResolver<STATE, CONTEXT> {
    override fun resolve(
        chatDefinition: ChatDefinition<STATE, CONTEXT>,
        update: Update,
        chat: Chat<STATE, CONTEXT>?,
        dialog: Dialog<STATE, CONTEXT>?,
        transitionDefinition: TransitionDefinition<STATE, CONTEXT>?
    ): ActionContext<STATE, CONTEXT> {
        var buttonTypeName: String? = null
        var buttonText: String? = null
        var triggerData: TriggerData? = null

        update.callbackQuery?.data?.let { encodedCallbackData ->
            when (val callbackData = callbackDataService.decode(encodedCallbackData)) {
                is ButtonData -> {
                    buttonTypeName = callbackData.buttonTypeName
                    buttonText = callbackData.buttonText
                }

                is TriggerData -> {
                    triggerData = callbackData
                }
            }
        }

        val user = update.message?.from
            ?: update.callbackQuery?.from
            ?: throw IllegalStateException("Cannot resolve user")

        val tgChat = update.message?.chat
            ?: (update.callbackQuery?.message as? org.telegram.telegrambots.meta.api.objects.Message)?.chat
            ?: throw IllegalStateException("Cannot resolve chat: callbackQuery.message inaccessible")

        return ActionContext(
            update = update,
            text = update.message?.text,
            context = dialog?.context ?: chatDefinition.contextInitializer(),
            from = dialog?.currentState,
            to = transitionDefinition?.thenDefinition?.to,
            eventType = eventTypeMatcher.match(update),
            user = user,
            chat = tgChat,
            buttonTypeName = buttonTypeName,
            buttonText = buttonText,
            _triggerContext = triggerData?.triggerContext,
            chatName = chatDefinition.name,
            dialogId = dialog?.id,
            repliedToMessageId = update.message?.replyToMessage?.messageId,
            languageCode = chat?.languageCode,
            photos = update.message?.photo,
            document = update.message?.document
        )
    }

}