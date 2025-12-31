package kz.rmr.chatmachinist.model

data class TriggerData(
    val triggerChatName: String,
    val triggerChatId: Long,
    val triggerContext: Any?,
    val triggerDialogId: String?,
)