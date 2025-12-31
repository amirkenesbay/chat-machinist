package kz.rmr.chatmachinist.builders

import kz.rmr.chatmachinist.api.transition.*
import kz.rmr.chatmachinist.model.*

class ChatBuilderImpl<STATE : Any, CONTEXT : Any> : ChatBuilder<STATE, CONTEXT> {

    override var name: String? = null

    private val dialogs = mutableListOf<DialogBuilderImpl<STATE, CONTEXT>>()
    private var commandsBuilder: CommandsBuilderImpl<STATE, CONTEXT>? = null
    private var contextInitializer: ContextInitializer<CONTEXT>? = null

    override fun dialog(init: DialogBuilder<STATE, CONTEXT>.() -> Unit): DialogBuilder<STATE, CONTEXT> {
        val transitionBuilder = DialogBuilderImpl<STATE, CONTEXT>()
        transitionBuilder.init()
        dialogs.add(transitionBuilder)
        return transitionBuilder
    }

    override fun initialContext(contextInitializer: ContextInitializer<CONTEXT>) {
        this.contextInitializer = contextInitializer
    }

    override fun commands(init: CommandsBuilder<STATE, CONTEXT>.() -> Unit): CommandsBuilder<STATE, CONTEXT> {
        val commandsBuilder = CommandsBuilderImpl<STATE, CONTEXT>()
        commandsBuilder.init()
        this.commandsBuilder = commandsBuilder
        return commandsBuilder
    }

    fun build(): ChatDefinition<STATE, CONTEXT> {
        val name = name ?: throw IllegalStateException("Chat name must be specified")
        val contextInitializer =
            contextInitializer ?: throw IllegalStateException("Context initializer must be specified in chat $name")
        if (dialogs.isEmpty()) {
            throw IllegalStateException("Dialogs must be specified in chat $name")
        }
        return ChatDefinition(
            name,
            contextInitializer,
            dialogs.map { dialogBuilder ->
                dialogBuilder.build(name)
            },
            commandsBuilder?.build() ?: emptyList(),
        )
    }
}

class CommandsBuilderImpl<STATE : Any, CONTEXT : Any> : CommandsBuilder<STATE, CONTEXT> {

    private var commandBuilders = mutableListOf<CommandBuilderImpl<STATE, CONTEXT>>()

    override fun command(init: CommandBuilderImpl<STATE, CONTEXT>.() -> Unit): CommandBuilderImpl<STATE, CONTEXT> {
        val commandBuilder = CommandBuilderImpl<STATE, CONTEXT>()
        commandBuilder.init()
        this.commandBuilders.add(commandBuilder)
        return commandBuilder
    }

    fun build(): List<CommandDefinition> = commandBuilders.map { it.build() }
}

class CommandBuilderImpl<STATE : Any, CONTEXT : Any> : CommandBuilderApi<STATE, CONTEXT> {

    override var text: String? = null
    override var description: String? = null
    override var scope: BotCommandScope? = null

    fun build() = CommandDefinition(
        text ?: throw IllegalStateException("Command text must be specified"),
        description ?: throw IllegalStateException("Command description must be specified"),
        scope ?: BotCommandScope.DEFAULT
    )
}

class DialogBuilderImpl<STATE : Any, CONTEXT : Any> : DialogBuilder<STATE, CONTEXT> {

    private val transitions = mutableListOf<TransitionBuilderImpl<STATE, CONTEXT>>()

    override var name: String? = null

    override fun transition(init: TransitionBuilder<STATE, CONTEXT>.() -> Unit): TransitionBuilder<STATE, CONTEXT> {
        val transitionBuilder = TransitionBuilderImpl<STATE, CONTEXT>()
        transitionBuilder.init()
        transitions.add(transitionBuilder)
        return transitionBuilder
    }

    fun build(chatName: String): DialogDefinition<STATE, CONTEXT> {
        val name = name ?: throw IllegalStateException("Dialog name must be specified")
        if (transitions.isEmpty()) {
            throw IllegalStateException("Transitions must be specified in dialog $name in chat $chatName")
        }
        val startDialogTransitions = transitions.filter { it.startDialog }.toList()
        if (startDialogTransitions.isEmpty()) {
            throw IllegalStateException("Start dialog transition must be specified in dialog $name in chat $chatName")
        }
        if (startDialogTransitions.size > 1) {
            throw IllegalStateException("Only one start dialog transition must be specified in dialog $name in chat $chatName")
        }
        return DialogDefinition(
            name,
            transitions.map { transitionBuilder ->
                transitionBuilder.build(chatName, name)
            })
    }
}


class ConditionBuilderImpl<STATE : Any, CONTEXT : Any> : ConditionBuilder<STATE, CONTEXT> {
    override var eventType: EventType? = null
    override var eventTypes: List<EventType>? = null
    override var from: STATE? = null
    override var button: Enum<*>? = null
    override var text: String? = null
    override var repliedToMessage: Boolean? = null

    private var guard: Guard<STATE, CONTEXT>? = null

    override fun guard(guard: Guard<STATE, CONTEXT>) {
        this.guard = guard
    }

    fun build(
        chatName: String,
        dialogName: String,
        transitionName: String
    ): DesiredConditionDefinition<STATE, CONTEXT> {
        if (eventType != null && eventTypes != null) {
            throw IllegalStateException("Only one event type can be specified in transition $transitionName in dialog $dialogName in chat $chatName")
        }

        val finalEventTypes = (eventType?.let { listOf(it) } ?: eventTypes ?: emptyList()).toMutableList()

        if (button != null && finalEventTypes.isNotEmpty() && !finalEventTypes.contains(EventType.INLINE_BUTTON_CLICKED)) {
            throw IllegalStateException("Button can be specified only for inline button clicked event type in transition $transitionName in dialog $dialogName in chat $chatName")
        }

        if (button != null && !finalEventTypes.contains(EventType.INLINE_BUTTON_CLICKED)) {
            finalEventTypes.add(EventType.INLINE_BUTTON_CLICKED)
        }

        if (finalEventTypes.isEmpty()) {
            throw IllegalStateException("Event type must be specified in transition $transitionName in dialog $dialogName in chat $chatName")
        }

        return DesiredConditionDefinition(
            eventTypes = finalEventTypes,
            from = from,
            buttonType = button,
            text = text,
            guard = guard,
            repliedToMessage = repliedToMessage,
        )
    }
}


class ThenBuilderImpl<STATE : Any, CONTEXT : Any> : ThenBuilder<STATE, CONTEXT> {
    override var to: STATE? = null
    override var noReply: Boolean = false
    private var triggerBuilder: TriggerBuilderImpl<STATE, CONTEXT>? = null

    override fun trigger(init: TriggerBuilder<STATE, CONTEXT>.() -> Unit): TriggerBuilder<STATE, CONTEXT> {
        val triggerBuilder = TriggerBuilderImpl<STATE, CONTEXT>()
        triggerBuilder.init()
        this.triggerBuilder = triggerBuilder
        return triggerBuilder
    }

    fun build(
        chatName: String,
        dialogName: String,
        transitionName: String
    ) = ThenDefinition(
        to
            ?: throw IllegalStateException("To state must be specified in transition $transitionName in dialog $dialogName in chat $chatName"),
        triggerBuilder?.build(chatName, dialogName, transitionName),
        noReply
    )
}
typealias TriggerChatNameResolver<STATE, CONTEXT> = ActionContext<STATE, CONTEXT>.() -> String

class TriggerBuilderImpl<STATE : Any, CONTEXT : Any> : TriggerBuilder<STATE, CONTEXT> {
    override var chatName: String? = null
    override var sameDialog: Boolean = false

    private var triggerContextBuilder: TriggerContextBuilder<STATE, CONTEXT, *>? = null
    private var triggerChatIdResolver: TriggerChatIdResolver<STATE, CONTEXT>? = null
    private var triggerDialogIdResolver: TriggerDialogIdResolver<STATE, CONTEXT>? = null
    private var triggerChatNameResolver: TriggerChatNameResolver<STATE, CONTEXT>? = null

    override fun <TRIGGER_CONTEXT> triggerContext(triggerContextBuilder: TriggerContextBuilder<STATE, CONTEXT, TRIGGER_CONTEXT>) {
        this.triggerContextBuilder = triggerContextBuilder
    }

    override fun chatId(triggerChatIdResolver: TriggerChatIdResolver<STATE, CONTEXT>) {
        this.triggerChatIdResolver = triggerChatIdResolver
    }

    override fun dialogId(triggerDialogIdResolver: TriggerDialogIdResolver<STATE, CONTEXT>) {
        this.triggerDialogIdResolver = triggerDialogIdResolver
    }

    fun build(
        chatDefinitionName: String,
        dialogName: String,
        transitionName: String
    ): TriggerDefinition<STATE, CONTEXT> {
        triggerChatNameResolver = if (this.chatName != null) {
            {
                this@TriggerBuilderImpl.chatName!!
            }
        } else if (sameDialog) {
            {
                chatName
            }
        } else {
            throw IllegalStateException("Chat name or same dialog must be specified in trigger $transitionName in dialog $dialogName in chat $chatDefinitionName")
        }
        if (sameDialog) {
            triggerChatIdResolver = {
                chat.id
            }
            triggerDialogIdResolver = {
                dialogId
                    ?: throw IllegalStateException("Dialog id must be specified in trigger $transitionName in dialog $dialogName in chat $chatDefinitionName")
            }
        }


        return TriggerDefinition(
            triggerChatNameResolver!!, triggerContextBuilder, triggerChatIdResolver!!, triggerDialogIdResolver
        )
    }
}

class TransitionBuilderImpl<STATE : Any, CONTEXT : Any> : TransitionBuilder<STATE, CONTEXT> {
    override var startDialog: Boolean = false
    override var name: String? = null

    private var conditionBuilders: MutableList<ConditionBuilderImpl<STATE, CONTEXT>> = mutableListOf()
    private var thenBuilder: ThenBuilderImpl<STATE, CONTEXT>? = null
    private var action: Action<STATE, CONTEXT>? = null

    override fun action(action: Action<STATE, CONTEXT>) {
        this.action = action
    }

    override fun condition(init: ConditionBuilder<STATE, CONTEXT>.() -> Unit): ConditionBuilder<STATE, CONTEXT> {
        val builder = ConditionBuilderImpl<STATE, CONTEXT>()
        builder.init()
        conditionBuilders.add(builder)
        return builder
    }

    override fun then(init: ThenBuilder<STATE, CONTEXT>.() -> Unit): ThenBuilder<STATE, CONTEXT> {
        val builder = ThenBuilderImpl<STATE, CONTEXT>()
        builder.init()
        thenBuilder = builder
        return builder
    }

    fun build(chatName: String, dialogName: String): TransitionDefinition<STATE, CONTEXT> {
        val name = name
            ?: throw IllegalStateException("Transition name must be specified in dialog $dialogName in chat $chatName")
        if (conditionBuilders.isEmpty()) {
            throw IllegalStateException("At least one condition must be specified in transition $name in dialog $dialogName in chat $chatName")
        }

        if (thenBuilder == null) {
            throw IllegalStateException("Then definition must be specified in transition $name in dialog $dialogName in chat $chatName")
        }

        return TransitionDefinition(
            name,
            startDialog = startDialog,
            action = action,
            desiredConditions = conditionBuilders.map { it.build(chatName, dialogName, name) },
            thenDefinition = thenBuilder!!.build(chatName, dialogName, name)
        )
    }
}