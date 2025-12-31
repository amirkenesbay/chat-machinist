package kz.rmr.chatmachinist.persistence.inmemory.repository

import kz.rmr.chatmachinist.model.UpdateResponse
import kz.rmr.chatmachinist.persistence.UpdateResponseRepository

class InMemoryUpdateResponseRepository<STATE : Enum<STATE>, CONTEXT : Any>: UpdateResponseRepository<STATE, CONTEXT> {

    private val memory = mutableListOf<UpdateResponse>()
    private var idGenerator = 0

    override fun save(it: UpdateResponse): UpdateResponse {
        memory.removeIf { it.id == it.id }
        memory.add(it)

        if (it.id != null) {
            return it
        }

        idGenerator++
        return it.copy(id = idGenerator.toString())
    }
}