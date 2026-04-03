package kz.rmr.chatmachinist.service.impl

import kz.rmr.chatmachinist.ChatMachinistProperties
import kz.rmr.chatmachinist.model.BotCommandScope
import kz.rmr.chatmachinist.service.TelegramClient
import mu.KotlinLogging
import org.telegram.telegrambots.bots.DefaultAbsSender
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.commands.DeleteMyCommands
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember
import org.telegram.telegrambots.meta.api.methods.ActionType
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction
import org.telegram.telegrambots.meta.api.methods.send.SendDocument
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember
import org.telegram.telegrambots.meta.api.objects.commands.scope.*
import kotlin.reflect.jvm.jvmName

class TelegramClientImpl(chatMachinistProperties: ChatMachinistProperties) : TelegramClient, DefaultAbsSender(
    DefaultBotOptions(), chatMachinistProperties.bot.token
) {
    private val logger = KotlinLogging.logger(this::class.jvmName)

    override fun sendMessage(message: SendMessage): Int {
        return execute(message).messageId
    }

    override fun editMessage(editMessage: EditMessageText) {
        execute(editMessage)
    }

    override fun deleteMessage(deleteMessage: DeleteMessage) {
        execute(deleteMessage)
    }

    override fun answerCallbackQuery(answerCallbackQuery: AnswerCallbackQuery) {
        execute(answerCallbackQuery)
    }

    override fun deleteMyCommands() {
        execute(DeleteMyCommands())
    }

    override fun setMyCommands(commands: List<kz.rmr.chatmachinist.model.BotCommand>, scope: BotCommandScope) {
        execute(SetMyCommands().apply {
            setCommands(commands.map { mapBotCommand(it) })
            setScope(mapScope(scope))
        })
    }

    private fun mapBotCommand(command: kz.rmr.chatmachinist.model.BotCommand) =
        org.telegram.telegrambots.meta.api.objects.commands.BotCommand(command.text, command.description)

    private fun mapScope(scope: BotCommandScope): org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScope {
        return when (scope) {
            BotCommandScope.PRIVATE_CHAT -> BotCommandScopeAllPrivateChats()
            BotCommandScope.DEFAULT -> BotCommandScopeDefault()
            BotCommandScope.GROUP_CHAT -> BotCommandScopeAllGroupChats()
            BotCommandScope.ADMINISTRATORS_ALL_CHAT -> BotCommandScopeAllChatAdministrators()
            BotCommandScope.ADMINISTRATORS_CHAT -> BotCommandScopeChatAdministrators()
            BotCommandScope.MEMBER_CHAT -> BotCommandScopeChatMember()
            BotCommandScope.CHAT -> BotCommandScopeChat()
        }
    }

    override fun getChatMember(getChatMemberRequest: GetChatMember?): ChatMember? {
        return execute(getChatMemberRequest)
    }

    override fun sendPhoto(message: SendPhoto) {
        execute(message)
    }

    override fun sendDocument(document: SendDocument) {
        execute(document)
    }

    override fun sendChatAction(chatId: String, action: String) {
        val sendChatAction = SendChatAction()
        sendChatAction.chatId = chatId
        sendChatAction.setAction(ActionType.get(action))
        execute(sendChatAction)
    }

}