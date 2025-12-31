package kz.rmr.chatmachinist.model

data class CallbackData(
    val id: String? = null,
    val encodedData: String,
    val callbackData: Any,
)
