package kz.rmr.chatmachinist.service.impl

import kz.rmr.chatmachinist.model.ButtonData
import kz.rmr.chatmachinist.service.CallbackDataService

class InMemoryCallbackDataService : CallbackDataService {

    private val callbackDataMap = mutableMapOf<String, Any>()

    override fun encode(callbackData: Any): String {
        val encodedCallbackData = callbackData.hashCode().toString()
        callbackDataMap[encodedCallbackData] = callbackData
        return encodedCallbackData
    }

    override fun decode(encodedCallbackData: String): Any {
        val decoded =  callbackDataMap[encodedCallbackData]
            ?: throw IllegalArgumentException("No callback data found for $encodedCallbackData")

        if (decoded is String) {
            val data = decoded.split("---")
            return ButtonData(data[0], data[1])
        }
        return decoded
    }

    fun existsCallbackData(callbackData: Any): Boolean {
        return callbackDataMap.containsValue(callbackData)
    }
}