package kz.rmr.chatmachinist.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import kz.rmr.chatmachinist.model.ButtonData
import kz.rmr.chatmachinist.model.TriggerData
import kz.rmr.chatmachinist.model.UpdateResponse
import kz.rmr.chatmachinist.service.AlertService
import kz.rmr.chatmachinist.service.CallbackDataService
import kz.rmr.chatmachinist.service.EventTypeMatcher
import kz.rmr.chatmachinist.service.TelegramClient
import kz.rmr.chatmachinist.utils.chatId
import kz.rmr.chatmachinist.utils.user
import org.springframework.beans.factory.annotation.Value
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import java.util.*

class TelegramAlertService(
    private val telegramClient: TelegramClient,
    private val eventTypeMatcher: EventTypeMatcher,
    private val callbackDataService: CallbackDataService,
) : AlertService {

    @Value("\${chat-machinist.alert.telegramChatId}")
    private lateinit var alertChatId: String

    @Value("\${chat-machinist.applicationName:#{null}}")
    private lateinit var applicationName: Optional<String>

    private val objectMapper = ObjectMapper().writerWithDefaultPrettyPrinter()

    override fun alert(updateResponse: UpdateResponse) {
        val appName = applicationName.orElse("Unknown app")

        var buttonTypeName: String? = null
        var buttonText: String? = null
        var triggerData: TriggerData? = null

        updateResponse.update.callbackQuery?.data?.let { encodedCallbackData ->
            when (val callbackData = callbackDataService.decode(encodedCallbackData)) {
                is ButtonData -> {
                    buttonTypeName = callbackData.buttonTypeName
                    buttonText = callbackData.buttonText
                }

                is TriggerData -> {
                    triggerData = callbackData
                }
            }
        }


        val text = """
Error occurred in $appName
            
Status: ${updateResponse.status}
ChatId: ${updateResponse.update.chatId()}
Username: ${updateResponse.update.user().userName}
            
Dialog: 
    Name: ${updateResponse.matchedTransition?.dialog?.name}
    Status: ${updateResponse.matchedTransition?.dialog?.currentState}
    Context: ${objectMapper.writeValueAsString(updateResponse.matchedTransition?.dialog?.context)}
                          
Transition: ${updateResponse.matchedTransition?.transitionDefinition?.name}
            
Update:
    Text: ${updateResponse.update.message?.text}
    EventType: ${eventTypeMatcher.match(updateResponse.update)}  
    Button: 
        Type: $buttonTypeName
        Text: $buttonText

TriggerData: ${objectMapper.writeValueAsString(triggerData)} 
""".trimIndent()

        val sendMessage = SendMessage().apply {
            this.text = text
            this.chatId = alertChatId
        }

        telegramClient.sendMessage(sendMessage)
    }
}