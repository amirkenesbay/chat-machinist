package kz.rmr.chatmachinist

import kz.rmr.chatmachinist.service.UpdateHandler
import org.telegram.telegrambots.bots.TelegramWebhookBot
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.objects.Update

class ChatMachinistWebhookBot(
    private val machinistProperties: ChatMachinistProperties,
    private val updateHandler: UpdateHandler,
) : TelegramWebhookBot() {
    override fun getBotUsername() = machinistProperties.bot.name

    override fun getBotToken() = machinistProperties.bot.token

    override fun onWebhookUpdateReceived(update: Update): BotApiMethod<*>? {
        updateHandler.handle(update)
        return null
    }

    override fun getBotPath() = machinistProperties.bot.webhook?.botPath

}