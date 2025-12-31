package kz.rmr.chatmachinist

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "chat-machinist")
data class ChatMachinistProperties(
    var bot: BotProperties
)

data class BotProperties(
    var name: String,
    var token: String,
    var webhook: Webhook?
)

data class Webhook(
    var url: String,
    var botPath: String
)