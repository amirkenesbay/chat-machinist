package kz.rmr.chatmachinist.service

interface CallbackDataService {

    fun encode(callbackData: Any): String

    fun decode(encodedCallbackData: String): Any
}