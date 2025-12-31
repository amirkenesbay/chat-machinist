package kz.rmr.chatmachinist.service.impl

import jakarta.annotation.PostConstruct
import kz.rmr.chatmachinist.ChatMachinistProperties
import kz.rmr.chatmachinist.service.BotFactory
import mu.KotlinLogging
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook
import org.telegram.telegrambots.meta.generics.LongPollingBot
import org.telegram.telegrambots.meta.generics.TelegramBot
import org.telegram.telegrambots.meta.generics.Webhook
import org.telegram.telegrambots.meta.generics.WebhookBot
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import java.lang.IllegalStateException
import kotlin.reflect.jvm.jvmName


class BotFactoryImpl(
    private val myBot: TelegramBot,
    private val chatMachinistProperties: ChatMachinistProperties,
    private val webhook: Webhook?
) : BotFactory {
    private val logger = KotlinLogging.logger(this::class.jvmName)

    @PostConstruct
    fun postConstruct() {
        createBot()
    }

    private fun createBot() {
        when {
            myBot is LongPollingBot && webhook == null -> {
                logger.info { "Creating LongPollingBot" }
                val telegramBotsApi = TelegramBotsApi(DefaultBotSession::class.java)

                telegramBotsApi.registerBot(myBot)
            }

            myBot is WebhookBot && webhook != null -> {
                logger.info { "Creating WebhookBot" }
                val telegramBotsApi = TelegramBotsApi(DefaultBotSession::class.java, webhook)
                val setWebhook = SetWebhook().apply {
                    url = chatMachinistProperties.bot.webhook!!.url
                }
                telegramBotsApi.registerBot(myBot, setWebhook)
            }

            else -> throw IllegalStateException("Unknown state for creating a bot")
        }
    }
}