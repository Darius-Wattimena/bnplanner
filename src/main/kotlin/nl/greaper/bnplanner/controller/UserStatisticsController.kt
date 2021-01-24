package nl.greaper.bnplanner.controller

import mu.KotlinLogging
import nl.greaper.bnplanner.model.Statistics
import nl.greaper.bnplanner.model.UserStatistics
import nl.greaper.bnplanner.model.UserStatistics.StatisticsType
import nl.greaper.bnplanner.service.StatisticsService
import nl.greaper.bnplanner.service.UserStatisticsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/user/statistics")
class UserStatisticsController(
        val service: UserStatisticsService
) {
    private val log = KotlinLogging.logger {}

    @GetMapping("/find")
    fun find(
            @RequestParam osuId: Long,
            @RequestParam start: Long,
            @RequestParam end: Long,
            @RequestParam type: StatisticsType
    ): List<UserStatistics> {
        return try {
           service.find(osuId, start, end, type)
        } catch (ex: Exception) {
            log.error("Error while executing Request", ex)
            emptyList()
        }
    }
}