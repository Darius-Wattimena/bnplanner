package nl.greaper.bnplanner.controller

import nl.greaper.bnplanner.dataSource.BeatmapDataSource
import nl.greaper.bnplanner.model.HealthCheck
import nl.greaper.bnplanner.model.filter.BeatmapFilter
import nl.greaper.bnplanner.scheduled.StatisticsCalculations
import nl.greaper.bnplanner.service.OsuService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthCheckController(
        val statisticsCalculations: StatisticsCalculations,
        val beatmapDataSource: BeatmapDataSource,
        val osuService: OsuService
) {

    @GetMapping("/")
    fun healthCheck(): HealthCheck {
        return HealthCheck("200")
    }

}