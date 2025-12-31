package kz.rmr.chatmachinist.model

import org.telegram.telegrambots.meta.api.objects.Update

data class ChatHandleResult<STATE, CONTEXT>(
    val success: Boolean,
    val triggerChat: Boolean = false,
    val triggerChatUpdate: Update? = null,
    val matchedTransition: MatchedTransition<STATE, CONTEXT>? = null,
    val replyResult: ReplyResult? = null,
) {
    companion object {
        fun <STATE, CONTEXT> success(matchedTransition: MatchedTransition<STATE, CONTEXT>, replyResult: ReplyResult?) =
            ChatHandleResult(
                true,
                matchedTransition = matchedTransition,
                replyResult = replyResult
            )

        fun <STATE, CONTEXT> notHandled() = ChatHandleResult<STATE, CONTEXT>(false)

        fun <STATE, CONTEXT> triggered(
            matchedTransition: MatchedTransition<STATE, CONTEXT>,
            replyResult: ReplyResult?,
            update: Update
        ) = ChatHandleResult(
            true,
            triggerChat = true,
            triggerChatUpdate = update,
            matchedTransition = matchedTransition,
            replyResult = replyResult
        )
    }
}