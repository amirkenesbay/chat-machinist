package kz.rmr.chatmachinist.builders

import kz.rmr.chatmachinist.api.reply.*
import kz.rmr.chatmachinist.model.*
import kz.rmr.chatmachinist.widget.calendarRows
import kz.rmr.chatmachinist.widget.yearPickerRows
import kz.rmr.chatmachinist.widget.monthPickerRows
import java.util.Locale

class RepliesBuilderImpl<STATE : Enum<STATE>, CONTEXT : Any> : RepliesBuilder<STATE, CONTEXT> {
    override var chatName: String? = null

    private val replies = mutableListOf<ReplyBuilderImpl<STATE, CONTEXT>>()

    override fun reply(init: ReplyBuilder<STATE, CONTEXT>.() -> Unit): ReplyBuilder<STATE, CONTEXT> {
        val transitionBuilder = ReplyBuilderImpl<STATE, CONTEXT>()
        transitionBuilder.init()
        replies.add(transitionBuilder)
        return transitionBuilder
    }

    fun build(): RepliesDefinition<STATE, CONTEXT> {
        val chatName = chatName ?: throw IllegalStateException("chatName is not set in replies")

        if (replies.isEmpty()) throw IllegalStateException("replies is empty in replies for chat $chatName")

        return RepliesDefinition(
            chatName,
            replies.map { it.build(chatName) }
        )
    }
}

class ReplyBuilderImpl<STATE : Enum<STATE>, CONTEXT : Any> : ReplyBuilder<STATE, CONTEXT> {

    override var state: STATE? = null

    private var messageGenerator: MessageGenerator<STATE, CONTEXT>? = null

    override fun message(init: MessageBuilder<STATE, CONTEXT>.() -> Unit) {
        messageGenerator = MessageGenerator { actionContext ->
            val messageBuilder = MessageBuilderImpl(actionContext.context, actionContext)
            messageBuilder.init()
            messageBuilder.build(state!!)
        }
    }

    fun build(chatName: String): ReplyDefinition<STATE, CONTEXT> {
        val state = state ?: throw IllegalStateException("state is not set in reply for chat $chatName")
        return ReplyDefinition(
            state,
            messageGenerator
                ?: throw IllegalStateException("message is not set in reply for state $state for chat $chatName"),
        )
    }
}

class MessageBuilderImpl<STATE : Enum<STATE>, CONTEXT : Any>(
    override val context: CONTEXT,
    override val actionContext: ActionContext<STATE, CONTEXT>
) : MessageBuilder<STATE, CONTEXT> {
    override var text: String? = null
    override var textCode: String? = null
    override var newMessage: Boolean = false
    override var newPinnedMessage: Boolean = false
    override var parseMode = ParseMode.DEFAULT
    override var disableLinkPreview: Boolean? = null


    private var keyboardBuilder: KeyboardBuilderImpl<STATE, CONTEXT>? = null
    private var photoBuilder: PhotoBuilderImpl<STATE, CONTEXT>? = null
    private var documentBuilder: DocumentBuilderImpl<STATE, CONTEXT>? = null

    override fun photo(init: PhotoBuilder<STATE, CONTEXT>.() -> Unit): PhotoBuilder<STATE, CONTEXT> {
        val photoBuilder = PhotoBuilderImpl<STATE, CONTEXT>()
        photoBuilder.init()
        this.photoBuilder = photoBuilder
        return photoBuilder
    }

    override fun document(init: DocumentBuilder<STATE, CONTEXT>.() -> Unit): DocumentBuilder<STATE, CONTEXT> {
        val documentBuilder = DocumentBuilderImpl<STATE, CONTEXT>()
        documentBuilder.init()
        this.documentBuilder = documentBuilder
        return documentBuilder
    }


    override fun keyboard(init: KeyboardBuilder<STATE, CONTEXT>.() -> Unit): KeyboardBuilder<STATE, CONTEXT> {
        val keyboardBuilder = KeyboardBuilderImpl<STATE, CONTEXT>()
        keyboardBuilder.init()
        this.keyboardBuilder = keyboardBuilder
        return keyboardBuilder
    }

    fun build(state: STATE): MessageDefinition {
        if (text.isNullOrBlank() && textCode.isNullOrBlank()) {
            throw IllegalStateException("text is not set in message for state $state")
        }
        if (text != null && textCode != null) {
            throw IllegalStateException("text and textCode are both set in message for state $state")
        }

        return MessageDefinition(
            text,
            textCode,
            keyboardDefinition = keyboardBuilder?.build(state),
            photoDefinition = photoBuilder?.build(),
            documentDefinition = documentBuilder?.build(),
            newMessage = newMessage,
            newPinnedMessage = newPinnedMessage,
            parseMode = parseMode,
            disableLinkPreview = disableLinkPreview
        )
    }
}

class KeyboardBuilderImpl<STATE : Enum<STATE>, CONTEXT : Any> : KeyboardBuilder<STATE, CONTEXT> {

    override var inline: Boolean = true

    private var buttonRowBuilders: MutableList<ButtonRowBuilderImpl<STATE, CONTEXT>> = mutableListOf()
    private var extraRows: MutableList<ButtonRowDefinition> = mutableListOf()

    override fun buttonRow(init: ButtonRowBuilder<*, *>.() -> Unit): ButtonRowBuilder<*, *> {
        val buttonRowBuilder = ButtonRowBuilderImpl<STATE, CONTEXT>()
        buttonRowBuilder.init()
        buttonRowBuilders.add(buttonRowBuilder)
        return buttonRowBuilder
    }

    override fun calendar(year: Int, month: Int, locale: Locale) {
        extraRows.addAll(calendarRows(year, month, locale))
    }

    override fun yearPicker(currentYear: Int) {
        extraRows.addAll(yearPickerRows(currentYear))
    }

    override fun monthPicker(year: Int, locale: Locale) {
        extraRows.addAll(monthPickerRows(year, locale))
    }

    fun build(state: STATE): KeyboardDefinition {
        return KeyboardDefinition(inline, buttonRowBuilders.map { it.build(state) } + extraRows)
    }
}

class ButtonRowBuilderImpl<STATE : Enum<STATE>, CONTEXT : Any> : ButtonRowBuilder<STATE, CONTEXT> {

    private val buttonBuilders: MutableList<ButtonBuilderImpl<STATE, CONTEXT>> = mutableListOf()

    override fun button(init: ButtonBuilder<STATE, CONTEXT>.() -> Unit): ButtonBuilder<STATE, CONTEXT> {
        val buttonBuilder = ButtonBuilderImpl<STATE, CONTEXT>()
        buttonBuilder.init()
        buttonBuilders.add(buttonBuilder)
        return buttonBuilder
    }

    fun build(state: STATE): ButtonRowDefinition {
        return ButtonRowDefinition(buttonBuilders.map { it.build(state) })
    }
}


class ButtonBuilderImpl<STATE : Enum<STATE>, CONTEXT : Any> : ButtonBuilder<STATE, CONTEXT> {

    override var text: String? = null
    override var textCode: String? = null
    override var type: Enum<*>? = null
    override var link: String? = null
    override var data: String? = null

    fun build(state: STATE): ButtonDefinition {
        if (text == null && textCode == null) {
            throw IllegalStateException("text or textCode is not set in button for state $state")
        }
        if (type == null) {
            throw IllegalStateException("type is not set in button for state $state")
        }
        return ButtonDefinition(text, textCode, type!!, link, data)
    }
}


class PhotoBuilderImpl<STATE : Enum<STATE>, CONTEXT : Any> : PhotoBuilder<STATE, CONTEXT> {
    override var fileId: String? = null
    fun build(): PhotoDefinition {
        return PhotoDefinition(
            fileId!!,
        )
    }
}

class DocumentBuilderImpl<STATE : Enum<STATE>, CONTEXT : Any> : DocumentBuilder<STATE, CONTEXT> {
    override var fileId: String? = null
    fun build(): DocumentDefinition {
        return DocumentDefinition(
            fileId!!,
        )
    }
}