package kz.rmr.chatmachinist.persistence.inmemory.repository

import kz.rmr.chatmachinist.model.CallbackData
import kz.rmr.chatmachinist.persistence.CallbackDataRepository

class InMemoryCallbackDataRepository : CallbackDataRepository {

    private val memory = mutableListOf<CallbackData>()
    private var idGenerator = 0
    override fun findAll(): List<CallbackData> {
        return memory
    }

    override fun findByEncodedData(encodedData: String): CallbackData? {
        return memory.find {
            it.encodedData == encodedData
        }
    }

    override fun save(callbackData: CallbackData): CallbackData {
        memory.removeIf { it.id == callbackData.id }
        memory.add(callbackData)

        if (callbackData.id != null) {
            return callbackData
        }

        idGenerator++
        return callbackData.copy(id = idGenerator.toString())
    }
}