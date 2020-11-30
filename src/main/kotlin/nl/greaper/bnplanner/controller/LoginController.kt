package nl.greaper.bnplanner.controller

import mu.KotlinLogging
import nl.greaper.bnplanner.OsuHttpClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/login")
class LoginController (val client: OsuHttpClient) {

    private val log = KotlinLogging.logger {}

    @PostMapping("/withToken")
    fun getToken(@RequestBody token: String): String {
        return try {
            client.getToken(token.removeSurrounding("\"")).body ?: ""
        } catch (ex: Exception) {
            log.error("Error while executing Request", ex)
            ""
        }
    }
}