package kz.rmr.chatmachinist.service.impl

import kz.rmr.chatmachinist.model.*
import kz.rmr.chatmachinist.service.CallbackDataService
import kz.rmr.chatmachinist.service.ContextResolver
import kz.rmr.chatmachinist.service.TransitionMatcher
import mu.KotlinLogging
import org.telegram.telegrambots.meta.api.objects.Update
import java.util.*
import kotlin.reflect.jvm.jvmName

class TransitionMatcherImpl<STATE : Any, CONTEXT : Any>(
    private val contextResolver: ContextResolver<STATE, CONTEXT>,
    private val callbackDataService: CallbackDataService,
) : TransitionMatcher<STATE, CONTEXT> {

    private val logger = KotlinLogging.logger(this::class.jvmName)

    override fun match(
        chatDefinition: ChatDefinition<STATE, CONTEXT>,
        update: Update,
        chat: Chat<STATE, CONTEXT>
    ): MatchedTransition<STATE, CONTEXT>? {
        var triggerDialogId: String? = null
        if (update.callbackQuery?.data != null) {
            val callbackData = callbackDataService.decode(update.callbackQuery.data)
            if (callbackData is TriggerData) {
                if (chatDefinition.name != callbackData.triggerChatName) {
                    logger.debug { "The update cannot be handled in chat ${chatDefinition.name}" }
                    return null
                }
                triggerDialogId = callbackData.triggerDialogId
            }
        }
        return chatDefinition
            .dialogDefinitions
            .firstNotNullOfOrNull { dialogDefinition: DialogDefinition<STATE, CONTEXT> ->

                val actionContext = contextResolver.resolve(
                    chatDefinition,
                    update,
                    chat = null,
                    dialog = null,
                    transitionDefinition = null
                )

                matchTransition(chatDefinition, dialogDefinition, existingDialog = null, actionContext)
                    ?.let { startTransitionDefinition ->
                        val dialog = defaultDialog(dialogDefinition, chatDefinition)
                        return@firstNotNullOfOrNull MatchedTransition(
                            dialog = dialog,
                            transitionDefinition = startTransitionDefinition,
                            contextResolver.resolve(
                                chatDefinition,
                                update,
                                chat = chat,
                                dialog = dialog,
                                transitionDefinition = startTransitionDefinition,
                            )
                        )
                    }


                chat.dialogs
                    .let { dialogs ->
                        if (triggerDialogId != null) {
                            dialogs.filter { it.id == triggerDialogId }
                        } else dialogs
                    }
                    .filter { currentDialog ->
                        val actionContext = contextResolver.resolve(
                            chatDefinition,
                            update,
                            chat = chat,
                            dialog = currentDialog,
                            transitionDefinition = null
                        )

                        currentDialog
                            .takeIf {
                                it.name == dialogDefinition.name
                            }
                            ?.takeIf {
                                if (update.callbackQuery?.message?.messageId == null) {
                                    true
                                } else it.botMessageIds.contains(update.callbackQuery?.message?.messageId)
                            }
                            ?.takeIf {
                                if (update.message?.replyToMessage?.messageId == null) {
                                    true
                                } else it.botMessageIds.contains(update.message?.replyToMessage?.messageId)
                            }
                            ?.takeIf {
                                if (actionContext.dialogId == null) {
                                    true
                                } else actionContext.dialogId == it.id
                            } != null
                    }
                    .firstNotNullOfOrNull { currentDialog ->
                        val actionContext = contextResolver.resolve(
                            chatDefinition,
                            update,
                            chat = chat,
                            dialog = currentDialog,
                            transitionDefinition = null
                        )
                        val transitionDefinition = matchTransition(
                            chatDefinition,
                            dialogDefinition,
                            existingDialog = currentDialog,
                            actionContext
                        )
                        if (transitionDefinition == null) {
                            null
                        } else {
                            Pair(currentDialog, transitionDefinition)
                        }
                    }
                    ?.let { (currentDialog, transitionDefinition) ->
                        val actionContext = contextResolver.resolve(
                            chatDefinition,
                            update,
                            chat = chat,
                            dialog = currentDialog,
                            transitionDefinition = transitionDefinition,
                        )
                        MatchedTransition(
                            dialog = currentDialog,
                            transitionDefinition = transitionDefinition,
                            actionContext = actionContext,
                        )
                    }
            }
    }

    private fun defaultDialog(
        dialogDefinition: DialogDefinition<STATE, CONTEXT>,
        chatDefinition: ChatDefinition<STATE, CONTEXT>,
    ) = Dialog<STATE, CONTEXT>(
        id = null,
        name = dialogDefinition.name,
        currentState = null,
        botMessageIds = mutableListOf(),
        pinnedMessageId = null,
        context = chatDefinition.contextInitializer(),
    )

    private fun matchTransition(
        chatDefinition: ChatDefinition<STATE, CONTEXT>,
        dialogDefinition: DialogDefinition<STATE, CONTEXT>,
        existingDialog: Dialog<STATE, CONTEXT>?,
        actionContext: ActionContext<STATE, CONTEXT>,
    ): TransitionDefinition<STATE, CONTEXT>? {
        return dialogDefinition.transitionDefinitions
            .firstOrNull { transitionDefinition ->
                match(chatDefinition, transitionDefinition, existingDialog, actionContext)
            }
    }

    private fun match(
        chatDefinition: ChatDefinition<STATE, CONTEXT>,
        transitionDefinition: TransitionDefinition<STATE, CONTEXT>,
        existingDialog: Dialog<STATE, CONTEXT>?,
        actionContext: ActionContext<STATE, CONTEXT>,
    ): Boolean {
        if (existingDialog == null && !transitionDefinition.startDialog) {
            return false
        }

        return transitionDefinition.desiredConditions.any { desiredCondition ->
            matchDesiredCondition(
                transitionDefinition,
                actionContext,
                desiredCondition,
                chatDefinition,
                existingDialog,
                actionContext.repliedToMessageId
            )
        }
    }

    private fun matchDesiredCondition(
        transitionDefinition: TransitionDefinition<STATE, CONTEXT>,
        actionContext: ActionContext<STATE, CONTEXT>,
        desiredCondition: DesiredConditionDefinition<STATE, CONTEXT>,
        chatDefinition: ChatDefinition<STATE, CONTEXT>,
        existingDialog: Dialog<STATE, CONTEXT>?,
        repliedToMessageId: Int?
    ): Boolean {
        val statusMatch = transitionDefinition.startDialog
            ||
            (actionContext.from == desiredCondition.from)

        if (!statusMatch) {
            logger.debug {
                "Did not match Transition \"${transitionDefinition.name}\" in chat ${chatDefinition.name}\n" +
                    "wrong status: ${actionContext.from} != ${desiredCondition.from}"
            }
            return false
        }


        val eventTypeMatch = if (desiredCondition.eventTypes.isEmpty()) {
            true
        } else {
            desiredCondition.eventTypes.contains(actionContext.eventType)
        }

        if (!eventTypeMatch) {
            logger.debug {
                "Did not match Transition \"${transitionDefinition.name}\" in chat ${chatDefinition.name}\n" +
                    "wrong event type: ${actionContext.eventType} not in the list of ${desiredCondition.eventTypes}"
            }
            return false
        }
        val buttonMatch = Objects.equals(actionContext.buttonTypeName, desiredCondition.buttonType?.name)
        if (!buttonMatch) {
            logger.debug {
                "Did not match Transition \"${transitionDefinition.name}\" in chat ${chatDefinition.name}\n" +
                    "wrong button type: ${actionContext.buttonTypeName} != ${desiredCondition.buttonType?.name}"
            }
            return false
        }
        val textMatch = if (desiredCondition.text == null) true else Objects.equals(
            actionContext.text,
            desiredCondition.text
        )
        if (!textMatch) {
            logger.debug {
                "Did not match Transition \"${transitionDefinition.name}\" in chat ${chatDefinition.name}\n" +
                    "wrong text: ${actionContext.text} != ${desiredCondition.text}"
            }
            return false
        }

        val repliedToMessageMatch = desiredCondition.repliedToMessage
            ?.let { desiredRepliedToMessage: Boolean ->
                if (existingDialog == null) {
                    return@let !desiredRepliedToMessage
                }

                if (actionContext.repliedToMessageId == null) {
                    return@let !desiredRepliedToMessage
                }

                repliedToMessageId in existingDialog.botMessageIds
            } ?: true

        if (!repliedToMessageMatch) {
            logger.debug {
                "Did not match Transition \"${transitionDefinition.name}\" in chat ${chatDefinition.name}\n" +
                    "wrong repliedToMessage: ${actionContext.repliedToMessageId} != ${existingDialog?.botMessageIds}"
            }
            return false
        }

        val guardMatch = desiredCondition.guard?.let {
            it(actionContext)
        } ?: true

        if (!guardMatch) {
            logger.debug {
                "Did not match Transition \"${transitionDefinition.name}\" in chat ${chatDefinition.name}\n" +
                    "guard did not match"
            }
            return false
        }

        logger.info { "Matched Transition \"${transitionDefinition.name}\" in chat ${chatDefinition.name}" }
        return true
    }
}