package kz.rmr.chatmachinist.model

import kz.rmr.chatmachinist.api.transition.ContextInitializer


data class ChatDefinition<STATE: Any, CONTEXT: Any>(
    val name: String,
    val contextInitializer: ContextInitializer<CONTEXT>,
    val dialogDefinitions: List<DialogDefinition<STATE, CONTEXT>>,
    val commandDefinitions: List<CommandDefinition>,
) {
}

data class DialogDefinition<STATE, CONTEXT>(
    val name: String,
    val transitionDefinitions: List<TransitionDefinition<STATE, CONTEXT>>
)

