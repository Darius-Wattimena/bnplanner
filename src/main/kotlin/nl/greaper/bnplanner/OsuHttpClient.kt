package nl.greaper.bnplanner

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.natpryce.konfig.Configuration
import nl.greaper.bnplanner.model.osu.Me
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class OsuHttpClient(val config: Configuration) {
    private val rest = RestTemplate()
    private val headers = HttpHeaders()

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