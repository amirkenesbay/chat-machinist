package kz.rmr.chatmachinist.model

data class RepliesDefinition<STATE, CONTEXT>(
    val chatName: String,
    val replies: List<ReplyDefinition<STATE, CONTEXT>>
)