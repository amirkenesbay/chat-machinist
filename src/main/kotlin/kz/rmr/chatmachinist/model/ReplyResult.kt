package kz.rmr.chatmachinist.model

import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod

data class ReplyResult(
    val apiMethods: List<BotApiMethod<*>>,
//    val partialApiMethods: List<PartialBotApiMethod<*>>,
)