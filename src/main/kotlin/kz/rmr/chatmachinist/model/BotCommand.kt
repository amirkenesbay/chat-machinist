package kz.rmr.chatmachinist.model

data class BotCommand(val text: String, val description: String, val scope: BotCommandScope)