package kz.rmr.chatmachinist.service.impl

import kz.rmr.chatmachinist.model.*
import kz.rmr.chatmachinist.service.ChatDefinitionVisualizer

class FlowChartChatDefinitionVisualizer<STATE : Any, CONTEXT : Any> : ChatDefinitionVisualizer<STATE, CONTEXT> {

    // if there are multiple transitions from one state, then we need to create a diamond
    // EXAMPLE:
    // flowchart TD
    //    START --> diamond1{branching}
    //    diamond1 -->|condition1| STARTED
    //    diamond1 ---->|condition2| NO_ACCESS
    // here in the diamond1{branching} "branching" is a hardcoded word
    // and condition1 and condition2 are the actual condition descriptions like trigger guarded or button APPROVE_ACTIVITY_REQUEST

    // if there is only one transition from one state to another, then we don't need a diamond
    // just a simple arrow
    // Example:
    // flowchart TD
    //   START -->|condition1| STARTED

    private var diamondCounter = 1

    override fun visualize(chatDefinition: ChatDefinition<STATE, CONTEXT>): List<DialogVisualization> {
        diamondCounter = 1
        return chatDefinition.dialogDefinitions
            .map { dialogDefinition ->
                val sb = StringBuilder()
                sb.append("---\n" +
                    "title: ${dialogDefinition.name}\n" +
                    "---\n")
                sb.append("flowchart TD\n")

                transitionsFrom(
                    from = "START",
                    dialogDefinition = dialogDefinition,
                    handledStates = mutableListOf()
                )
                    .forEach {
                        sb.append(it)
                    }

                sb.toString()

                DialogVisualization(
                    chatName = chatDefinition.name,
                    dialogName = dialogDefinition.name,
                    mermaid = sb.toString()
                )
            }
    }

    private fun transitionsFrom(
        from: String,
        dialogDefinition: DialogDefinition<STATE, CONTEXT>,
        handledStates: MutableList<String>
    ): List<String> {
        if (handledStates.contains(from)) {
            return emptyList()
        }
        handledStates.add(from)

        val transitions = dialogDefinition.transitionDefinitions
            .filter {
                if (from == "START") {
                    it.startDialog
                } else {
                    it.desiredConditions.any { it.from.toString() == from  }
                }
            }

        if (transitions.isEmpty()) {
            return listOf(
                "   $from --> END\n"
            )
        }

        if (transitions.size == 1) {
            val transition = transitions.first()
            val result = transition.desiredConditions.map { desiredCondition ->
                transitionLine(
                    transition,
                    desiredCondition
                )
            }.toMutableList()
            val nextTransitions = transitionsFrom(transition.thenDefinition.to.toString(), dialogDefinition, handledStates)
            result.addAll(nextTransitions)
            return result
        }

        val result = mutableListOf<String>()
        val diamondName = "diamond$diamondCounter"
        diamondCounter++
        result.add("$from --o $diamondName{$from branching}\n")
        val extraTransitions = transitions.flatMap { transitionDefinition ->
            transitionDefinition.desiredConditions.map { desiredCondition ->
                transitionLine(transitionDefinition, desiredCondition, diamondName)
            }
        }

        result.addAll(extraTransitions)

        transitions.forEach { transition ->
            val nextTransitions = transitionsFrom(transition.thenDefinition.to.toString(), dialogDefinition, handledStates)
            result.addAll(nextTransitions)
        }

        return result
    }

    private fun transitionLine(
        transition: TransitionDefinition<STATE, CONTEXT>,
        desiredCondition: DesiredConditionDefinition<STATE, CONTEXT>,
        diamondName: String? = null
    ): String {
        val from = diamondName ?: desiredCondition.from ?: "START"
        val to = transition.thenDefinition.to

        var condition: String = desiredCondition.eventTypes.joinToString(separator = " or ") {
            conditionFromEventType(it, desiredCondition)
        }

        if (desiredCondition.guard != null) {
            condition += " guarded"
        }

        if (desiredCondition.repliedToMessage == true) {
            condition += " repliedToMessage"
        }

        if (condition.isNotBlank()) {
            return "   $from ==>|\"$condition\"| $to\n"
        } else {
            return "   $from ==> $to\n"
        }
    }

    private fun conditionFromEventType(
        eventType: EventType,
        desiredConditionDefinition: DesiredConditionDefinition<STATE, CONTEXT>
    ) = when (eventType) {
        EventType.INLINE_BUTTON_CLICKED -> "button ${desiredConditionDefinition.buttonType ?: ""}"
        EventType.TRIGGERED -> "trigger"
        EventType.TEXT -> "text ${desiredConditionDefinition.text ?: ""}"
        EventType.COMMAND -> "command"
        EventType.DOCUMENT -> "document"
        EventType.PHOTO -> "photo"
        EventType.WEB_APP_DATA -> "webAppData"
    }
}
