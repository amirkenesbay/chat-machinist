package kz.rmr.chatmachinist.persistence

import kz.rmr.chatmachinist.model.UpdateResponse

interface UpdateResponseRepository<STATE : Enum<STATE>, CONTEXT : Any>: Repository<UpdateResponse> {

    override fun save(updateResponse: UpdateResponse): UpdateResponse
}