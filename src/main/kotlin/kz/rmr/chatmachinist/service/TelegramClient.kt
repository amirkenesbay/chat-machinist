package kz.rmr.chatmachinist.service

import kz.rmr.chatmachinist.model.BotCommandScope
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember
import org.telegram.telegrambots.meta.api.methods.send.SendDocument
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember


interface TelegramClient {

    fun sendMessage(message: SendMessage): Int
    fun editMessage(editMessage: EditMessageText)
    fun deleteMessage(deleteMessage: DeleteMessage)
    fun answerCallbackQuery(answerCallbackQuery: AnswerCallbackQuery)
    fun deleteMyCommands()
    fun setMyCommands(commands: List<kz.rmr.chatmachinist.model.BotCommand>, scope: BotCommandScope)
    fun getChatMember(getChatMemberRequest: GetChatMember?): ChatMember?
    fun sendPhoto(message: SendPhoto)
    fun sendDocument(document: SendDocument)
}