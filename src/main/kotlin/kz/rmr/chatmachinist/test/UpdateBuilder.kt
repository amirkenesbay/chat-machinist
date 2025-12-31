package kz.rmr.chatmachinist.test

import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User

class UpdateBuilder(
    val user: User,
    val chat: Chat
) {

    fun fromText(text: String): Update {
        return Update().apply {
            message = Messages.fromText(text, user, chat)
        }
    }


    private object Messages {
        fun fromText(text: String, user: User, chat: Chat): Message {
            return Message().apply {
                this.text = text
                this.from = user
                this.chat = chat
            }
        }
    }
}