package nl.greaper.bnplanner.controller

import nl.greaper.bnplanner.dataSource.BeatmapDataSource
import nl.greaper.bnplanner.model.HealthCheck
import nl.greaper.bnplanner.model.filter.BeatmapFilter
import nl.greaper.bnplanner.scheduled.StatisticsCalculations
import nl.greaper.bnplanner.service.OsuService
import org.springframework.web.bind.annotation.GetMapping
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

    @GetMapping("/generateUserStatistics")
    fun generateUserStatistics(): String {
        statisticsCalculations.calculateDailyUserStatistics()
        statisticsCalculations.calculateMonthlyUserStatistics()
        statisticsCalculations.calculateYearlyUserStatistics()

        return "Done calculating"
    }

    @GetMapping("/assignCreatorIdsToAllBeatmaps")
    fun assignCreatorIdsToAllBeatmaps(): String {
        val allBeatmaps = beatmapDataSource.findAll(BeatmapFilter(
                artist = null,
                title = null,
                mapper = null,
                mapperId = null,
                countTotal = false,
                asStatistics = true,
                limit = null,
                page = null
        ))

        allBeatmaps.response.forEach { beatmap ->
            var beatmapWithMapperId = beatmap
            val osuBeatmapSet = osuService.findBeatmapSetInfo(
                    "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI2NjYiLCJqdGkiOiJjMDYyMmFmOTY4OGU1YzU2MTk1MDJjZGM5OTBmZmM5NzEzYzFhMjQxMzk3MjcxNDNlOGJjMTRiODcwZTVlNjJjYTRkMGI4OGYxODFiMzhhYiIsImlhdCI6MTYxMTQxMTA4NCwibmJmIjoxNjExNDExMDg0LCJleHAiOjE2MTE0OTc0ODQsInN1YiI6IjIzNjk3NzYiLCJzY29wZXMiOlsiaWRlbnRpZnkiLCJwdWJsaWMiXX0.fxOl2ZCmsXJsGuueRfXKfwtQTSYpRoAQ0TN0x7J4yVBpXxjwravUsObHUE5uXkGOSiSXrM1QkO1LvlRFWsH50_KwoJBwUdxp0hF2JZ6QBK_7EKInPWUjCZ-3tzLNxEQbRhqQaJY4UAkooYCXoKsymDGw6Ep4HfiSemkjkJCckgIwN_TIEHvzFz_gGcfg4h_fGCVcKCeOU9nTaxaf1RMRk8VlDfXSTfvQxA8BWK-SYWxsqx1FyuFSnP3SCJmImkwI1yMSrpzTmtsbokh6PJKomHBxt0eqNn96t8xphEW9BhpTMnCoEE1eqUNVqlktSZmNfZKoJuPpbMV_VKqmf0ABQsOTRlMaQSVM9Ko6SOWxxLv0q6eL1jDvnMAugA2t2khMUgBCfXgBv-PDYbEnBe_jTvGiVrWOSZy1eCmNY6-Amyl4fVFBP52ZTlFp9QK20Fg8sSZZCpPLyfLEm2ZQUW5GFEzTKfWE5EO-bck6rkl753m9jhh9L6l7rZSZV7qU-SDpGFWZ5-Xs1H8kv7lfFVc87_hJH9dvJ44gSyiNwoGQWS4Q6QsnooqY06cAl49UuxEIQivVtnqJfRbPXKEykcA8elBKxW5Du4ofOYlk0TlAbrOMAWbuPttiONlqkXyNZXAP1BfEwytaeWShIbePz2jkpNvGLv7rNtG9IRpnEuTQWmU",
                    beatmap.osuId
            )

            beatmapWithMapperId.mapperId = osuBeatmapSet?.user_id ?: 0

            beatmapDataSource.save(beatmapWithMapperId)
        }

        return "Done giving all beatmaps an creator ID"
    }

}