package kz.rmr.chatmachinist.controller

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.updatesreceivers.ServerlessWebhook
import kotlin.reflect.jvm.jvmName

@RestController
@RequestMapping("callback")
class WebhookController(
    @Autowired(required = false)
    private val webhook: ServerlessWebhook?
) {
    private val logger = KotlinLogging.logger(this::class.jvmName)

    @PostMapping("{botPath}")
    fun webhookEndpoint(
        @PathVariable botPath: String,
        @RequestBody update: Update,
    ) {
        if (webhook == null) {
            logger.error { "Webhook is triggered but no handler is registered" }
            return
        }
        logger.info { "Webhook is called with update $update" }

        webhook.updateReceived(botPath, update)
    }


    @GetMapping("{botPath}")
    fun webhookTest(
        @PathVariable botPath: String
    ): String {
        if (webhook == null) {
            val errorMsg = "Webhook test is triggered but no handler is registered"
            logger.error { errorMsg }
            return errorMsg
        }
        return "Test webhook endpoint for $botPath"
    }
}