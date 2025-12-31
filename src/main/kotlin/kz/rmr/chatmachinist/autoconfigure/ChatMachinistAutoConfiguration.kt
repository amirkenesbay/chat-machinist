package kz.rmr.chatmachinist.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
import kz.rmr.chatmachinist.ChatMachinistLongPollingBot
import kz.rmr.chatmachinist.ChatMachinistProperties
import kz.rmr.chatmachinist.ChatMachinistWebhookBot
import kz.rmr.chatmachinist.builders.ChatBuilderImpl
import kz.rmr.chatmachinist.builders.RepliesBuilderImpl
import kz.rmr.chatmachinist.model.ChatDefinition
import kz.rmr.chatmachinist.model.DialogVisualization
import kz.rmr.chatmachinist.model.RepliesDefinition
import kz.rmr.chatmachinist.persistence.CallbackDataRepository
import kz.rmr.chatmachinist.persistence.ChatRepository
import kz.rmr.chatmachinist.persistence.UpdateResponseRepository
import kz.rmr.chatmachinist.service.*
import kz.rmr.chatmachinist.service.impl.*
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Lazy
import org.telegram.telegrambots.meta.generics.TelegramBot
import org.telegram.telegrambots.meta.generics.Webhook
import org.telegram.telegrambots.updatesreceivers.ServerlessWebhook
import kotlin.reflect.jvm.jvmName

@AutoConfiguration
@EnableConfigurationProperties(ChatMachinistProperties::class)
@ComponentScan("kz.rmr.chatmachinist.controller")
class ChatMachinistAutoConfiguration<STATE : Enum<STATE>, CONTEXT : Any> {

    private val logger = KotlinLogging.logger(this::class.jvmName)

    @Bean
    fun chatDefinition(chatBuilders: List<ChatBuilderImpl<STATE, CONTEXT>>): List<ChatDefinition<STATE, CONTEXT>> {
        return chatBuilders.map { it.build() }
    }

    @Bean
    fun replyDefinition(
        repliesBuilders: List<RepliesBuilderImpl<STATE, CONTEXT>>
    ): List<RepliesDefinition<STATE, CONTEXT>> {
        return repliesBuilders.map { it.build() }
    }

    @Bean
    @Lazy(false)
    fun commandRegister(
        chatDefinitions: List<ChatDefinition<STATE, CONTEXT>>,
        telegramClient: TelegramClient,
    ): CommandRegister {

        val commandDefinitions =
            chatDefinitions.firstOrNull() { it.commandDefinitions.isNotEmpty() }?.let { it.commandDefinitions }
                ?: emptyList()

        return CommandRegister(commandDefinitions, telegramClient)
    }


    @Bean
    fun chatService(chatRepository: ChatRepository<STATE, CONTEXT>): ChatService<STATE, CONTEXT> {
        return ChatServiceImpl(
            chatRepository
        )
    }

    @Bean
    fun eventTypeMatcher(
        callbackDataService: CallbackDataService
    ): EventTypeMatcher {
        return EventTypeMatcherImpl(callbackDataService)
    }

    @Bean
    fun messageSender(chatMachinistProperties: ChatMachinistProperties): TelegramClient {
        return TelegramClientImpl(chatMachinistProperties)
    }

    @Bean
    fun callbackDataService(
        callbackDataRepository: CallbackDataRepository
    ): CallbackDataService {
        return PersistenceCallbackDataService(
            callbackDataRepository,
            InMemoryCallbackDataService()
        )
    }

    @Bean
    fun contextResolver(
        callbackDataService: CallbackDataService,
        eventTypeMatcher: EventTypeMatcher
    ): ContextResolver<STATE, CONTEXT> {
        return ContextResolverImpl(
            callbackDataService,
            eventTypeMatcher
        )
    }

    @Bean
    fun transitionMatcher(
        contextResolver: ContextResolver<STATE, CONTEXT>,
        callbackDataService: CallbackDataService
    ): TransitionMatcher<STATE, CONTEXT> {
        return TransitionMatcherImpl(
            contextResolver,
            callbackDataService
        )
    }

    @Bean
    fun replyHandler(
        telegramClient: TelegramClient,
        callbackDataService: CallbackDataService,
        messageSource: MessageSource
    ): ReplyHandler<STATE, CONTEXT> {
        return ReplyHandlerImpl(
            telegramClient,
            callbackDataService,
            messageSource,
        )
    }

    @Bean
    fun triggerHandler(
        callbackDataService: CallbackDataService
    ): TriggerHandler<STATE, CONTEXT> {
        return TriggerHandlerImpl(
            callbackDataService
        )
    }

    @Bean
    fun chatUpdateHandler(
        chatService: ChatService<STATE, CONTEXT>,
        contextResolver: ContextResolver<STATE, CONTEXT>,
        transitionMatcher: TransitionMatcher<STATE, CONTEXT>,
        replyHandler: ReplyHandler<STATE, CONTEXT>,
        triggerHandler: TriggerHandler<STATE, CONTEXT>,
    ): ChatUpdateHandler<STATE, CONTEXT> {
        return ChatUpdateHandlerImpl(
            chatService,
            transitionMatcher,
            replyHandler,
            triggerHandler
        )
    }

    @Bean
    @ConditionalOnProperty("chat-machinist.alert.telegramChatId")
    fun telegramAlertService(
        telegramClient: TelegramClient,
        eventTypeMatcher: EventTypeMatcher,
        callbackDataService: CallbackDataService
    ): TelegramAlertService {
        return TelegramAlertService(telegramClient, eventTypeMatcher, callbackDataService)
    }

    @Bean
    fun updateHandler(
        chatDefinitions: List<ChatDefinition<STATE, CONTEXT>>,
        repliesDefinitions: List<RepliesDefinition<STATE, CONTEXT>>,
        chatUpdateHandler: ChatUpdateHandler<STATE, CONTEXT>,
        updateResponseRepository: UpdateResponseRepository<STATE, CONTEXT>,
        alertService: AlertService?
    ): UpdateHandler {
        return UpdateHandlerImpl(
            chatDefinitions,
            repliesDefinitions,
            chatUpdateHandler,
            updateResponseRepository,
            alertService
        )
    }

    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            writerWithDefaultPrettyPrinter()
        }
    }

    @Bean
    fun chatMachinistBot(
        machinistProperties: ChatMachinistProperties,
        updateHandler: UpdateHandler,
        @Autowired(required = false) webhook: ServerlessWebhook?,
        objectMapper: ObjectMapper
    ): TelegramBot {
        if (webhook != null) {
            return ChatMachinistWebhookBot(
                machinistProperties,
                updateHandler
            )
        }
        return ChatMachinistLongPollingBot(
            machinistProperties,
            updateHandler,
            objectMapper
        )
    }

    @Bean
    @ConditionalOnProperty("chat-machinist.bot.webhook.enable", havingValue = "true")
    fun webHook(
        chatMachinistProperties: ChatMachinistProperties
    ): ServerlessWebhook {
        return ServerlessWebhook()
    }

    @Bean
    @Lazy(false)
    fun botFactory(
        myBot: TelegramBot,
        chatMachinistProperties: ChatMachinistProperties,
        @Autowired(required = false) webhook: Webhook?
    ): BotFactory {
        return BotFactoryImpl(
            myBot,
            chatMachinistProperties,
            webhook
        )
    }

    @Bean
    fun visualizationPersister(): VisualizationPersister {
        return FileVisualizationPersister()
    }

    @Bean
    @Lazy(false)
    @ConditionalOnProperty(
        prefix = "chat-machinist",
        name = ["visualize"],
        havingValue = "true",
        matchIfMissing = false
    )
    fun chatDefinitionVisualizer(
        chatDefinitions: List<ChatDefinition<STATE, CONTEXT>>,
        visualizationPersister: VisualizationPersister
    ): ChatDefinitionVisualizer<STATE, CONTEXT> {
        return FlowChartChatDefinitionVisualizer<STATE, CONTEXT>()
            .apply {
                chatDefinitions
                    .flatMap { chatDefinition ->
                        visualize(chatDefinition)
                    }
                    .forEach { dialogVisualization: DialogVisualization ->
                        visualizationPersister.persist(dialogVisualization)
                    }
            }
    }
}