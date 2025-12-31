package kz.rmr.chatmachinist.model

import org.telegram.telegrambots.meta.api.objects.Update

data class UpdateResponse(
    val id: String?,
    val update: Update,
    val status: UpdateStatus,
    val exception: String?,
    val matchedTransition: MatchedTransition<*, *>?,
    val replyResult: ReplyResult?
)

enum class UpdateStatus {
    SUCCESS,
    ERROR,
    NOT_HANDLED,
    IGNORED,
}
