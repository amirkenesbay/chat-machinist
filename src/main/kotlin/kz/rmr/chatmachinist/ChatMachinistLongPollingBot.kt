package kz.rmr.chatmachinist

import com.fasterxml.jackson.databind.ObjectMapper
import kz.rmr.chatmachinist.service.UpdateHandler
import kz.rmr.chatmachinist.utils.chatId
import kz.rmr.chatmachinist.utils.userId
import mu.KotlinLogging
import org.slf4j.MDC
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import kotlin.reflect.jvm.jvmName

class ChatMachinistLongPollingBot(
    private val machinistProperties: ChatMachinistProperties,
    private val updateHandler: UpdateHandler,
    private val objectMapper: ObjectMapper
) : TelegramLongPollingBot() {

    override fun getBotUsername() = machinistProperties.bot.name
    override fun getBotToken() = machinistProperties.bot.token
    private val logger = KotlinLogging.logger(this::class.jvmName)

    override fun onUpdateReceived(update: Update) {
        MDC.put("chatId", update.chatId())
        MDC.put("userId", update.userId().toString())

        logger.debug { "Got update ${objectMapper.writeValueAsString(update)}" }
        updateHandler.handle(update)
    }
}