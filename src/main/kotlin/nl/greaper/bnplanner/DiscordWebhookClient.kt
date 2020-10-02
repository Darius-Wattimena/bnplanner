package nl.greaper.bnplanner

import com.fasterxml.jackson.databind.ObjectMapper
import com.natpryce.konfig.Configuration
import nl.greaper.bnplanner.config.KonfigConfiguration.discord
import nl.greaper.bnplanner.model.discord.*
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.time.Instant

@Component
class DiscordWebhookClient(
        val config: Configuration,
        val objectMapper: ObjectMapper
) {

    private val rest = RestTemplate()
    private val headers = HttpHeaders()

    fun send(
            description: String,
            color: EmbedColor,
            thumbnail: EmbedThumbnail,
            footer: EmbedFooter,
            confidential: Boolean = false
    ): ResponseEntity<String> {
        return send(EmbedMessage(
                description,
                Instant.now().toString(),
                color.getValue(),
                thumbnail,
                footer
        ), confidential)
    }

    private fun send(embedMessage: EmbedMessage, confidential: Boolean) : ResponseEntity<String> {
        headers.contentType = MediaType.APPLICATION_JSON
        // Private discord server with all messages
        val webhookUrl = config[discord.webhook]

        val body = objectMapper.writeValueAsString(Message(listOf(embedMessage)))
        val request = HttpEntity(body, headers)

        if (!confidential) {
            // Public catch mapping hub feed with only the most informative messages
            val moddingServerWebhookUrl = config[discord.moddingwebhook]

            if (moddingServerWebhookUrl.isNotBlank()) {
                rest.exchange(moddingServerWebhookUrl, HttpMethod.POST, request, String::class.java)
            }
        }

        return rest.exchange(webhookUrl, HttpMethod.POST, request, String::class.java)
    }
}