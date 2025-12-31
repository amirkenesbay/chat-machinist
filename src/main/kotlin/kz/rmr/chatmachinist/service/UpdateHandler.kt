package kz.rmr.chatmachinist.service

import kz.rmr.chatmachinist.model.UpdateResponse
import org.telegram.telegrambots.meta.api.objects.Update

interface UpdateHandler {
    fun handle(update: Update): List<UpdateResponse>
}