package nl.greaper.bnplanner.controller

import nl.greaper.bnplanner.model.HealthCheck
import nl.greaper.bnplanner.service.OsuService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthCheckController(
        val osuService: OsuService
) {
    @GetMapping("/")
    fun healthCheck(): HealthCheck {
        return HealthCheck("200")
    }
}