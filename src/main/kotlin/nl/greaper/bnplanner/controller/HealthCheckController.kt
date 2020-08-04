package nl.greaper.bnplanner.controller

import nl.greaper.bnplanner.model.HealthCheck
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1")
class HealthCheckController {

    @GetMapping("/healthcheck")
    fun healthCheck(): HealthCheck {
        return HealthCheck("100")
    }

}