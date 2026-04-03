package kz.rmr.chatmachinist.service.impl

import kz.rmr.chatmachinist.api.reply.ParseMode
import kz.rmr.chatmachinist.model.*
import kz.rmr.chatmachinist.service.CallbackDataService
import kz.rmr.chatmachinist.service.ReplyHandler
import kz.rmr.chatmachinist.service.TelegramClient
import mu.KotlinLogging
import org.springframework.context.MessageSource
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendDocument
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.util.*
import kotlin.reflect.jvm.jvmName

class ReplyHandlerImpl<STATE : Any, CONTEXT : Any>(
    private val telegramClient: TelegramClient,
    private val callbackDataService: CallbackDataService,
    private val messageSource: MessageSource,
) : ReplyHandler<STATE, CONTEXT> {

    private val logger = KotlinLogging.logger(this::class.jvmName)

    override fun handle(
        update: Update,
        chat: Chat<STATE, CONTEXT>,
        repliesDefinition: RepliesDefinition<STATE, CONTEXT>,
        matched: MatchedTransition<STATE, CONTEXT>,
    ): ReplyResult {
        val apiMethods = mutableListOf<BotApiMethod<*>>()
        val partialApiMethods = mutableListOf<PartialBotApiMethod<*>>()

        val reply = repliesDefinition.replies.firstOrNull {
            it.state == matched.transitionDefinition.thenDefinition.to
        }
            ?: throw IllegalStateException("Unable to find reply for state ${matched.transitionDefinition.thenDefinition.to}")

        val messageDefinition = reply.messageGenerator.generate(matched.actionContext)
        val parseMode = if (messageDefinition.parseMode == ParseMode.HTML) "HTML" else null
        val message = toMessage(messageDefinition, chat)
        val previousMessageId = update.callbackQuery?.message?.messageId ?: matched.dialog.pinnedMessageId

        if (previousMessageId != null && !messageDefinition.newMessage && !messageDefinition.newPinnedMessage) {
            editMessage(previousMessageId, message, chat, parseMode, messageDefinition, apiMethods)
        } else {
            sendMessage(message, chat, parseMode, messageDefinition, apiMethods, matched)
        }

        sendDocument(messageDefinition, chat, partialApiMethods)

        sendPhoto(messageDefinition, chat, partialApiMethods)

        answerCallbackQuery(update, apiMethods)
        return ReplyResult(apiMethods)
    }

    private fun sendPhoto(
        messageDefinition: MessageDefinition,
        chat: Chat<STATE, CONTEXT>,
        partialApiMethods: MutableList<PartialBotApiMethod<*>>
    ) {
        messageDefinition.photoDefinition?.let { photoDefinition ->
            val sendPhoto: SendPhoto = SendPhoto().apply {
                this.photo = InputFile(photoDefinition.fileId)
                this.chatId = chat.externalId
            }
            partialApiMethods.add(sendPhoto)
            telegramClient.sendPhoto(sendPhoto)
        }
    }

    private fun sendDocument(
        messageDefinition: MessageDefinition,
        chat: Chat<STATE, CONTEXT>,
        partialApiMethods: MutableList<PartialBotApiMethod<*>>
    ) {
        messageDefinition.documentDefinition?.let { docDefinition ->
            val sendDocument: SendDocument = SendDocument().apply {
                this.document = InputFile(docDefinition.fileId)
                this.chatId = chat.externalId
            }
            partialApiMethods.add(sendDocument)
            telegramClient.sendDocument(sendDocument)
        }
    }

    private fun sendMessage(
        message: Message,
        chat: Chat<STATE, CONTEXT>,
        parseMode: String?,
        messageDefinition: MessageDefinition,
        apiMethods: MutableList<BotApiMethod<*>>,
        matched: MatchedTransition<STATE, CONTEXT>
    ) {
        telegramClient.sendChatAction(chat.externalId, "typing")
        val sendMessage = SendMessage().apply {
            this.text = message.text
            this.replyMarkup = message.replyMarkup
            this.chatId = chat.externalId
            this.parseMode = parseMode
            this.disableWebPagePreview = messageDefinition.disableLinkPreview
        }
        apiMethods.add(sendMessage)
        val sentMessageId = telegramClient.sendMessage(sendMessage)

        matched.dialog.botMessageIds.add(sentMessageId)
        if (messageDefinition.newPinnedMessage) {
            if (matched.dialog.pinnedMessageId != null) {
                try {
                    telegramClient.deleteMessage(
                        DeleteMessage()
                            .apply { this.chatId = chat.externalId; this.messageId = matched.dialog.pinnedMessageId!! }
                    )
                } catch (e: Exception) {
                    logger.error("Could not delete previous pinned message", e)
                }
                matched.dialog.botMessageIds.remove(matched.dialog.pinnedMessageId!!)
            }
            matched.dialog.pinnedMessageId = sentMessageId
        }
        if (matched.dialog.pinnedMessageId == null) {
            matched.dialog.pinnedMessageId = sentMessageId
        }
    }

    private fun editMessage(
        previousMessageId: Int?,
        message: Message,
        chat: Chat<STATE, CONTEXT>,
        parseMode: String?,
        messageDefinition: MessageDefinition,
        apiMethods: MutableList<BotApiMethod<*>>
    ) {
        val editMessage = EditMessageText().apply {
            this.messageId = previousMessageId
            this.text = message.text
            this.replyMarkup = message.replyMarkup
            this.chatId = chat.externalId
            this.parseMode = parseMode
            this.disableWebPagePreview = messageDefinition.disableLinkPreview
        }
        apiMethods.add(editMessage)
        telegramClient.editMessage(editMessage)
    }

    private fun answerCallbackQuery(
        update: Update,
        apiMethods: MutableList<BotApiMethod<*>>
    ) {
        try {
            update.callbackQuery?.id?.let {
                val answerCallbackQuery = AnswerCallbackQuery().apply { setCallbackQueryId(it) }
                telegramClient.answerCallbackQuery(answerCallbackQuery)
                apiMethods.add(answerCallbackQuery)
            }
        } catch (e: Exception) {
            logger.warn("Unable to answer callback query", e)
        }
    }

    private fun toMessage(messageDefinition: MessageDefinition, chat: Chat<STATE, CONTEXT>): Message {
        return Message().apply {
            this.text = localizeText(messageDefinition.text, messageDefinition.textCode, chat.languageCode)

            messageDefinition.keyboardDefinition?.let {

                this.replyMarkup = InlineKeyboardMarkup().apply {

                    this.keyboard = it.buttonRowDefinitions.map { row ->
                        row.buttonDefinitions.map { buttonDefinition: ButtonDefinition ->

                            val buttonText = localizeText(buttonDefinition.text, buttonDefinition.textCode, chat.languageCode)

                            InlineKeyboardButton().apply {
                                this.text = buttonText
                                this.callbackData = callbackDataService.encode(ButtonData(buttonDefinition.type.name, buttonDefinition.data ?: buttonText))
                                this.url = buttonDefinition.link
                            }
                        }
                    }

                }
            }
        }
    }

    private fun localizeText(
        text: String?,
        textCode: String?,
        languageCode: String?
    ): String {
        if (textCode == null) {
            return text ?: throw IllegalStateException("Both text and textCode are null")
        }
        if (languageCode == null) {
            throw IllegalStateException("Language code is null")
        }
        val localized = messageSource.getMessage(textCode, null, Locale(languageCode))

        if (localized.isBlank()) {
            throw IllegalStateException("Localized text is blank for code $textCode")
        }

        return localized
    }
}