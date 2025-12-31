package kz.rmr.chatmachinist.model

data class Dialog<STATE, CONTEXT>(
    var id: String?,
    val name: String,
    var currentState: STATE?,
    val botMessageIds: MutableList<Int>,
    var pinnedMessageId: Int?,
    var context: CONTEXT
)