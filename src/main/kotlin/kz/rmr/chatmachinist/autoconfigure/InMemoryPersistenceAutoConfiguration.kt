package kz.rmr.chatmachinist.autoconfigure

import kz.rmr.chatmachinist.persistence.CallbackDataRepository
import kz.rmr.chatmachinist.persistence.ChatRepository
import kz.rmr.chatmachinist.persistence.UpdateResponseRepository
import kz.rmr.chatmachinist.persistence.inmemory.repository.InMemoryCallbackDataRepository
import kz.rmr.chatmachinist.persistence.inmemory.repository.InMemoryChatRepository
import kz.rmr.chatmachinist.persistence.inmemory.repository.InMemoryUpdateResponseRepository
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean

@AutoConfiguration
@ConditionalOnProperty("chat-machinist.persistence.type", havingValue = "in-memory", matchIfMissing = true)
class InMemoryPersistenceAutoConfiguration<STATE : Enum<STATE>, CONTEXT : Any> {

    @Bean
    fun callbackDataRepository(): CallbackDataRepository {
        return InMemoryCallbackDataRepository()
    }

    @Bean
    fun chatRepository(): ChatRepository<STATE, CONTEXT> {
        return InMemoryChatRepository()
    }

    @Bean
    fun updateResponseRepository(): UpdateResponseRepository<STATE, CONTEXT> {
        return InMemoryUpdateResponseRepository()
    }

}