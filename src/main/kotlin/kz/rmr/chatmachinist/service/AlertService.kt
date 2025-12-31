package kz.rmr.chatmachinist.service

import kz.rmr.chatmachinist.model.UpdateResponse

interface AlertService {

    fun alert(updateResponse: UpdateResponse)
}