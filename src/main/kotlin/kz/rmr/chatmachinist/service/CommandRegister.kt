package kz.rmr.chatmachinist.service

import jakarta.annotation.PostConstruct
import kz.rmr.chatmachinist.model.BotCommand
import kz.rmr.chatmachinist.model.CommandDefinition


class CommandRegister(
    private val commandDefinitions: List<CommandDefinition>,
    private val telegramClient: TelegramClient
) {

    @PostConstruct
    fun postConstruct() {
        if (commandDefinitions.isEmpty()) {
            return
        }
        telegramClient.deleteMyCommands()
        commandDefinitions
            .map { toBotCommand(it) }
            .groupBy { it.scope }
            .forEach { (scope, commands) -> telegramClient.setMyCommands(commands, scope) }
    }

    private fun toBotCommand(it: CommandDefinition) = BotCommand(it.text, it.description, it.scope)
}