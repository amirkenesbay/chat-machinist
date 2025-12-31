package kz.rmr.chatmachinist.model

data class CommandDefinition(
    val text: String,
    val description: String,
    val scope: BotCommandScope
)