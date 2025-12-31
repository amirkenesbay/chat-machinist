package kz.rmr.chatmachinist.service.impl

import kz.rmr.chatmachinist.exception.MatchedTransitionException
import kz.rmr.chatmachinist.model.ChatDefinition
import kz.rmr.chatmachinist.model.RepliesDefinition
import kz.rmr.chatmachinist.model.UpdateResponse
import kz.rmr.chatmachinist.model.UpdateStatus
import kz.rmr.chatmachinist.persistence.UpdateResponseRepository
import kz.rmr.chatmachinist.service.AlertService
import kz.rmr.chatmachinist.service.ChatUpdateHandler
import kz.rmr.chatmachinist.service.UpdateHandler
import mu.KotlinLogging
import org.telegram.telegrambots.meta.api.objects.Update
import kotlin.reflect.jvm.jvmName

class UpdateHandlerImpl<STATE : Enum<STATE>, CONTEXT : Any>(
    private val chatDefinitions: List<ChatDefinition<STATE, CONTEXT>>,
    private val repliesDefinitions: List<RepliesDefinition<STATE, CONTEXT>>,
    private val chatUpdateHandler: ChatUpdateHandler<STATE, CONTEXT>,
    private val updateResponseRepository: UpdateResponseRepository<STATE, CONTEXT>,
    private val alertService: AlertService?,
) : UpdateHandler {

    private val logger = KotlinLogging.logger(this::class.jvmName)

    override fun handle(update: Update): List<UpdateResponse> {
        return doHandle(update)
            .onEach { updateResponse ->
                alertErrorOrNotHandled(updateResponse)
            }
            .map {
                updateResponseRepository.save(it)
            }
    }

    private fun alertErrorOrNotHandled(
        updateResponse: UpdateResponse
    ) {
        if (updateResponse.status in listOf(
                UpdateStatus.ERROR,
                UpdateStatus.NOT_HANDLED,
            )
        ) {
            if (alertService != null) {
                alertService.alert(updateResponse)
            } else {
                logger.warn { "AlertService is not defined" }
            }
        }
    }

    private fun doHandle(update: Update): List<UpdateResponse> {
        return try {
            chatDefinitions.map {
                Pair(it, repliesDefinitions.first { repliesDefinition ->
                    repliesDefinition.chatName == it.name
                })
            }.firstNotNullOfOrNull { (chatDefinition, repliesDefinition) ->
                val result = chatUpdateHandler.handle(update, chatDefinition, repliesDefinition)
                if (!result.success) {
                    return@firstNotNullOfOrNull null
                }
                val responses = mutableListOf(
                    UpdateResponse(
                        id = null,
                        update = update,
                        status = UpdateStatus.SUCCESS,
                        matchedTransition = result.matchedTransition,
                        exception = null,
                        replyResult = result.replyResult
                    )
                )
                if (!result.triggerChat) {
                    return@firstNotNullOfOrNull responses
                }
                doHandle(result.triggerChatUpdate!!).let {
                    responses.addAll(it)
                }
                return@firstNotNullOfOrNull responses
            } ?: run {
                logger.warn { "Cannot handle update $update" }
                return listOf(
                    UpdateResponse(
                        id = null,
                        update = update,
                        status = UpdateStatus.NOT_HANDLED,
                        exception = null,
                        matchedTransition = null,
                        replyResult = null
                    )
                )
            }
        } catch (e: MatchedTransitionException) {
            logger.error(e) { "Error handling update $update" }
            return listOf(
                UpdateResponse(
                    id = null,
                    update = update,
                    status = UpdateStatus.ERROR,
                    exception = e.stackTraceToString(),
                    matchedTransition = e.matchedTransition,
                    replyResult = null
                )
            )
        }
    }


}