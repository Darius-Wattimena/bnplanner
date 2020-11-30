package nl.greaper.bnplanner

import com.fasterxml.jackson.databind.ObjectMapper
import com.natpryce.konfig.Configuration
import nl.greaper.bnplanner.config.KonfigConfiguration.osu
import nl.greaper.bnplanner.model.auth.OsuOAuth
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate


@Component
class OsuHttpClient(
        val config: Configuration,
        val objectMapper: ObjectMapper
) {
    private val rest = RestTemplate()
    private val authRest = RestTemplate()
    private val headers = HttpHeaders()
    private val authHeaders = HttpHeaders()

    init {
        authHeaders.contentType = MediaType.APPLICATION_JSON
        authHeaders.accept = listOf(MediaType.APPLICATION_JSON)
    }

    /**
     * Get a token from the osu server
     */
    fun getToken(code: String): ResponseEntity<String> {
        val uri = "https://osu.ppy.sh/oauth/token"

        val osuOAuth = OsuOAuth(
                config[osu.clientId].toInt(),
                config[osu.clientSecret],
                code,
                "authorization_code",
                config[osu.redirectUri]
        )

        val bodyAsJson = objectMapper.writeValueAsString(osuOAuth)
        val request = HttpEntity(bodyAsJson, authHeaders)

        return authRest.postForEntity(uri, request, String::class.java)
    }

    fun get(uri: String, authToken: String) : ResponseEntity<String> {
        return request(uri, HttpMethod.GET, authToken)
    }

    private fun request(uri: String, method: HttpMethod, authToken: String, body: String = "") : ResponseEntity<String> {
        headers.remove("Authorization")
        headers.set("Authorization", authToken)

        val request = if (body == "") {
            HttpEntity(headers)
        } else {
            HttpEntity(body, headers)
        }
        return rest.exchange("https://osu.ppy.sh/api/v2$uri", method, request, String::class.java)
    }
}