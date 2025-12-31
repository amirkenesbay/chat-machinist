package kz.rmr.chatmachinist.persistence

import kz.rmr.chatmachinist.model.CallbackData

interface CallbackDataRepository: Repository<CallbackData> {

    fun findAll(): List<CallbackData>

    fun findByEncodedData(encodedData: String): CallbackData?

    override fun save(it: CallbackData): CallbackData
}