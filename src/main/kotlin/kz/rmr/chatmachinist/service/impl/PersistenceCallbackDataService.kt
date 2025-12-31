package kz.rmr.chatmachinist.service.impl

import jakarta.annotation.PostConstruct
import kz.rmr.chatmachinist.model.CallbackData
import kz.rmr.chatmachinist.persistence.CallbackDataRepository
import kz.rmr.chatmachinist.service.CallbackDataService
import mu.KotlinLogging
import kotlin.reflect.jvm.jvmName

class PersistenceCallbackDataService(
    private val callbackDataRepository: CallbackDataRepository,
    private val inMemoryCallbackDataService: InMemoryCallbackDataService
): CallbackDataService by inMemoryCallbackDataService {

    private val logger = KotlinLogging.logger(this::class.jvmName)

    @PostConstruct
    fun init() {
        callbackDataRepository.findAll().forEach {
            inMemoryCallbackDataService.encode(it.callbackData)
        }
    }

    override fun encode(callbackData: Any): String {
        if (inMemoryCallbackDataService.existsCallbackData(callbackData)) {
            return inMemoryCallbackDataService.encode(callbackData)
        }

        val encoded = inMemoryCallbackDataService.encode(callbackData)

        logger.debug { "Persistence Callback Data. Searching for encoded values" }
        callbackDataRepository.findByEncodedData(encoded)?.let {
            return encoded
        }

        logger.debug { "Persistence Callback Data. Saving callback data" }
        callbackDataRepository.save(
            CallbackData(
                encodedData = encoded,
                callbackData = callbackData
            )
        )
        return encoded
    }
}