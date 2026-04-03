package kz.rmr.chatmachinist.api.reply

import kz.rmr.chatmachinist.api.DSLBuilder
import java.util.Locale

@DSLBuilder
interface KeyboardBuilder<STATE : Enum<STATE>, CONTEXT : Any> {
    var inline: Boolean

    fun buttonRow(init: ButtonRowBuilder<*, *>.() -> Unit): ButtonRowBuilder<*, *>

    fun calendar(year: Int, month: Int, locale: Locale = Locale("ru"))

    fun yearPicker(currentYear: Int)

    fun monthPicker(year: Int, locale: Locale = Locale("ru"))
}