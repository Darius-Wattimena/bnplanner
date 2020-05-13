package nl.greaper.bnplanner.controller

import nl.greaper.bnplanner.model.auth.UserProfile
import nl.greaper.bnplanner.service.OsuService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/osu")
class OsuController(val osuService: OsuService) {

    @GetMapping("/userInfo")
    fun getUserInfo(@RequestHeader(name = "Authorization") token: String): UserProfile? {
        return if (token != "Bearer undefined") {
            osuService.getUserInfo(token)
        } else {
            null
        }
    }
}