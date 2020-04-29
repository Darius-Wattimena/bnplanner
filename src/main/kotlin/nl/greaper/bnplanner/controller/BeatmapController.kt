package nl.greaper.bnplanner.controller

import mu.KotlinLogging
import nl.greaper.bnplanner.model.FindResponse
import nl.greaper.bnplanner.model.beatmap.*
import nl.greaper.bnplanner.model.filter.BeatmapFilter
import nl.greaper.bnplanner.service.AuthService
import nl.greaper.bnplanner.service.BeatmapService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/beatmap")
class BeatmapController(
        val service: BeatmapService,
        val authService: AuthService
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

    @GetMapping("/{id}/detailed")
    fun findDetailedBeatmap(@PathVariable("id") id: String): DetailedBeatmap? {
        return try {
            service.findDetailedBeatmap(id.toLong())
        } catch (ex: Exception) {
            log.error("Error while executing Request", ex)
            null
        }
    }

    @PutMapping("/{id}/update")
    fun updateBeatmap(
            @PathVariable("id") id: String,
            @RequestHeader(name = "Authorization") token: String,
            @RequestBody update: UpdatedBeatmap
    ): Boolean {
        return try {
            val user = authService.getUserFromAuthToken(token)
            if (user.hasEditPermissions) {
                service.updateBeatmap(user.osuId, id.toLong(), update)
                true
            } else {
                false
            }
        } catch (ex: Exception) {
            log.error("Error while executing Request", ex)
            false
        }
    }

    @PostMapping("/add")
    fun addBeatmap(
            @RequestHeader(name = "Authorization") token: String,
            @RequestBody newBeatmap: NewBeatmap
    ): Boolean {
        return try {
            val user = authService.getUserFromAuthToken(token)
            if (user.hasEditPermissions) {
                service.addBeatmap(
                        editorId = user.osuId,
                        beatmapId = newBeatmap.beatmapId.toLong(),
                        artist = newBeatmap.artist,
                        mapper = newBeatmap.mapper,
                        title = newBeatmap.title
                )
                true
            } else {
                false
            }
        } catch (ex: Exception) {
            log.error("Error while executing Request", ex)
            false
        }
    }

    @GetMapping("/searchByFilter")
    fun findBeatmaps(
            @RequestParam artist: String?,
            @RequestParam title: String?,
            @RequestParam mapper: String?,
            @RequestParam status: List<BeatmapStatus>?,
            @RequestParam limit: Int?,
            @RequestParam page: Int?,
            @RequestParam countTotal: Boolean?,
            @RequestParam hideGraved: Boolean?,
            @RequestParam hideRanked: Boolean?,
            @RequestParam nominator: List<Long>? = emptyList()
    ): FindResponse<FoundBeatmap> {
        return try {
            return service.findBeatmaps(BeatmapFilter(
                    artist,
                    title,
                    mapper,
                    limit,
                    page,
                    countTotal,
                    hideGraved,
                    hideRanked,
                    status ?: emptyList(),
                    nominator ?: emptyList()
            ))
        } catch (ex: Exception) {
            log.error("Error while executing Request", ex)
            FindResponse()
        }
    }
}