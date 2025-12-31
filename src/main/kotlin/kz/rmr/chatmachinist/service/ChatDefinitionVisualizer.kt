package kz.rmr.chatmachinist.service

import kz.rmr.chatmachinist.model.ChatDefinition
import kz.rmr.chatmachinist.model.DialogVisualization

interface ChatDefinitionVisualizer<STATE: Any, CONTEXT: Any> {

    fun visualize(chatDefinition: ChatDefinition<STATE, CONTEXT>): List<DialogVisualization>
}