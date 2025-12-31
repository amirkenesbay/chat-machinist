package kz.rmr.chatmachinist.model

data class ReplyDefinition<STATE, CONTEXT>(
    val state: STATE,
    val messageGenerator: MessageGenerator<STATE, CONTEXT>,
)

fun interface MessageGenerator<STATE, CONTEXT> {

    fun generate(actionContext: ActionContext<STATE, CONTEXT>): MessageDefinition
}
