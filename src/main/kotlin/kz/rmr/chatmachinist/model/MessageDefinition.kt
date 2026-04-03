package kz.rmr.chatmachinist.model

import kz.rmr.chatmachinist.api.reply.ParseMode

data class MessageDefinition(
    val text: String?,
    val textCode: String?,
    val keyboardDefinition: KeyboardDefinition?,
    val documentDefinition: DocumentDefinition?,
    val photoDefinition: PhotoDefinition?,
    val newMessage: Boolean,
    val newPinnedMessage: Boolean,
    val parseMode: ParseMode,
    val disableLinkPreview: Boolean?,
)

data class KeyboardDefinition(val inline: Boolean, val buttonRowDefinitions: List<ButtonRowDefinition>)

data class ButtonRowDefinition(val buttonDefinitions: List<ButtonDefinition>)

data class ButtonDefinition(val text: String?, val textCode: String?, val type: Enum<*>, val link: String?, val data: String? = null)