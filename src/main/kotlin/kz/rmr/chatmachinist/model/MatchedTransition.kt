package kz.rmr.chatmachinist.model

data class MatchedTransition<STATE, CONTEXT>(
    val dialog: Dialog<STATE, CONTEXT>,
    val transitionDefinition: TransitionDefinition<STATE, CONTEXT>,
    val actionContext: ActionContext<STATE, CONTEXT>
)
