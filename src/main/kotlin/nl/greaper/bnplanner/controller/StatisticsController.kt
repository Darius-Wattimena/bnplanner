package nl.greaper.bnplanner.controller

import mu.KotlinLogging
import nl.greaper.bnplanner.model.Statistics
import nl.greaper.bnplanner.service.StatisticsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/statistics")
class StatisticsController(
        val service: StatisticsService
) {
    private val log = KotlinLogging.logger {}

    @GetMapping("/latest")
    fun find(): Statistics? {
        return try {
           service.findLatest()
        } catch (ex: Exception) {
            log.error("Error while executing Request", ex)
            null
        }
    }
}