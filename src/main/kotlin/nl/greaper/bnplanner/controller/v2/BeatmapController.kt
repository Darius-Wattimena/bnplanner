package nl.greaper.bnplanner.controller.v2

import com.natpryce.konfig.Configuration
import mu.KotlinLogging
import nl.greaper.bnplanner.model.FindResponse
import nl.greaper.bnplanner.model.beatmap.Beatmap
import nl.greaper.bnplanner.model.beatmap.BeatmapPage
import nl.greaper.bnplanner.model.beatmap.FoundBeatmap
import nl.greaper.bnplanner.service.OsuService
import nl.greaper.bnplanner.service.v2.BeatmapService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v2/beatmap")
class BeatmapController(
    val config: Configuration,
    val service: BeatmapService,
    val osuService: OsuService
) {
    private val log = KotlinLogging.logger {}

    @GetMapping("/{id}")
    fun findBeatmap(@PathVariable("id") id: String): Beatmap? {
        return try {
            service.findBeatmap(id.toLong())
        } catch (ex: Exception) {
            log.error("Error while executing Request", ex)
            null
        }
    }

    @GetMapping("/countBeatmaps")
    fun countBeatmaps(
        @RequestParam(required = false) artist: String?,
        @RequestParam(required = false) title: String?,
        @RequestParam(required = false) mapper: String?,
        @RequestParam(required = false) status: List<Long> = emptyList(),
        @RequestParam(required = false) nominators: List<Long> = emptyList(),
        @RequestParam page: BeatmapPage
    ): Int {
        return try {
            service.countBeatmaps(artist, title, mapper, status, nominators, page)
        } catch (ex: Exception) {
            log.error("Error while executing Request", ex)
            0
        }
    }

    @GetMapping("/findBeatmaps")
    fun findBeatmaps(
        @RequestParam(required = false) artist: String?,
        @RequestParam(required = false) title: String?,
        @RequestParam(required = false) mapper: String?,
        @RequestParam(required = false) status: List<Long> = emptyList(),
        @RequestParam(required = false) nominators: List<Long> = emptyList(),
        @RequestParam page: BeatmapPage,
        @RequestParam from: Int,
        @RequestParam to: Int
    ): List<FoundBeatmap> {
        return try {
            service.findBeatmaps(artist, title, mapper, status, nominators, page, from, to)
        } catch (ex: Exception) {
            log.error("Error while executing Request", ex)
            emptyList()
        }
    }
}