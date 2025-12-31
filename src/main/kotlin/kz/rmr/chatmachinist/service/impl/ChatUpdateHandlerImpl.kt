package kz.rmr.chatmachinist.service.impl

import kz.rmr.chatmachinist.exception.MatchedTransitionException
import kz.rmr.chatmachinist.model.*
import kz.rmr.chatmachinist.service.*
import mu.KotlinLogging
import org.telegram.telegrambots.meta.api.objects.Update
import java.lang.Exception
import kotlin.reflect.jvm.jvmName

class ChatUpdateHandlerImpl<STATE : Any, CONTEXT : Any>(
    private val chatService: ChatService<STATE, CONTEXT>,
    private val transitionMatcher: TransitionMatcher<STATE, CONTEXT>,
    private val replyHandler: ReplyHandler<STATE, CONTEXT>,
    private val triggerHandler: TriggerHandler<STATE, CONTEXT>,
) : ChatUpdateHandler<STATE, CONTEXT> {

    private val logger = KotlinLogging.logger(this::class.jvmName)

    override fun handle(
        update: Update,
        chatDefinition: ChatDefinition<STATE, CONTEXT>,
        repliesDefinition: RepliesDefinition<STATE, CONTEXT>
    ): ChatHandleResult<STATE, CONTEXT> {
        logger.debug { "Trying to handle update in chat ${chatDefinition.name}" }
        val chat = findChatByUpdate(update, chatDefinition)
        val existingDialogsIds = chat.dialogs.map { it.id }

        var matched: MatchedTransition<STATE, CONTEXT>? = transitionMatcher.match(chatDefinition, update, chat)

        if (matched == null) {
            logger.debug { "The update cannot be handled in chat ${chatDefinition.name}" }
            return ChatHandleResult.notHandled()
        }

        try {
            matched.transitionDefinition.action?.let {
                it(matched!!.actionContext)
            }

            matched.dialog.context = matched.actionContext.context
            matched.dialog.currentState = matched.actionContext.to
            chat.languageCode = matched.actionContext.languageCode

            val replyResult = if (!matched.transitionDefinition.thenDefinition.noReply) {
                replyHandler.handle(update, chat, repliesDefinition, matched)
            } else {
                null
            }

            chat.dialogs.remove(matched.dialog)
            chat.dialogs.add(matched.dialog)

            val savedChat = chatService.save(chat)

            if (matched.transitionDefinition.thenDefinition.triggerDefinition != null) {

                val newDialog = savedChat.dialogs.find { it.id !in existingDialogsIds }
                if (newDialog != null) {
                    matched = matched.copy(actionContext = matched.actionContext.copy(dialogId = newDialog.id))
                }

                val triggerUpdate = triggerHandler.handle(matched.transitionDefinition, matched.actionContext)
                return ChatHandleResult.triggered(
                    update = triggerUpdate,
                    matchedTransition = matched,
                    replyResult = replyResult
                )
            }

            return ChatHandleResult.success(matched, replyResult)
        } catch (e: Exception) {
            throw MatchedTransitionException(matched, e)
        }
    }

    private fun findChatByUpdate(update: Update, chatDefinition: ChatDefinition<STATE, CONTEXT>): Chat<STATE, CONTEXT> {
        return chatService.getOrCreate(update, chatDefinition)
    }

}