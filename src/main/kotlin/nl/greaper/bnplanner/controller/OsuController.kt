package nl.greaper.bnplanner.controller

import mu.KotlinLogging
import nl.greaper.bnplanner.model.auth.UserProfile
import nl.greaper.bnplanner.service.OsuService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/osu")
class OsuController(val osuService: OsuService) {
    @GetMapping("/userInfo")
    fun getUserInfo(
            @RequestHeader(name = "Authorization") token: String
    ): UserProfile? {
        return if (token != "Bearer undefined") {
            osuService.getUserInfo(token)
        } else {
            null
        }
    }
}