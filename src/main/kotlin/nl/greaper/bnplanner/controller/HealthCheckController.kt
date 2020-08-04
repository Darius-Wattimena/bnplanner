package nl.greaper.bnplanner.controller

import nl.greaper.bnplanner.model.HealthCheck
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthCheckController {

    @GetMapping("/")
    fun healthCheck(): HealthCheck {
        return HealthCheck("200")
    }

}