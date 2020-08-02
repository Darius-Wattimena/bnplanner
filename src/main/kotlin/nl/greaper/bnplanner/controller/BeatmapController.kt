package nl.greaper.bnplanner.controller

import mu.KotlinLogging
import nl.greaper.bnplanner.model.FindResponse
import nl.greaper.bnplanner.model.beatmap.*
import nl.greaper.bnplanner.model.filter.BeatmapFilter
import nl.greaper.bnplanner.model.filter.BeatmapFilterLimit
import nl.greaper.bnplanner.service.BeatmapService
import nl.greaper.bnplanner.service.OsuService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/beatmap")
class BeatmapController(
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

    @GetMapping("/{id}/detailed")
    fun findDetailedBeatmap(@PathVariable("id") id: String): DetailedBeatmap? {
        return try {
            service.findDetailedBeatmap(id.toLong())
        } catch (ex: Exception) {
            log.error("Error while executing Request", ex)
            null
        }
    }

    @DeleteMapping("/{id}/delete")
    fun deleteBeatmap(
            @PathVariable("id") id: String,
            @RequestHeader(name = "Osu-Id") osuId: Long,
            @RequestHeader(name = "Authorization") token: String
    ): Boolean {
        return try {
            val user = osuService.getUserFromToken(token, osuId)
            if (user != null && user.hasEditPermissions) {
                service.deleteBeatmap(id.toLong())
                true
            } else {
                false
            }
        } catch (ex: Exception) {
            log.error("Error while executing Request", ex)
            false
        }
    }

    @PutMapping("/{id}/updateStatus")
    fun updateBeatmapStatus(
            @PathVariable("id") id: String,
            @RequestHeader(name = "Osu-Id") osuId: Long,
            @RequestHeader(name = "Authorization") token: String,
            @RequestBody status: UpdatedBeatmapStatus
    ): Boolean {
        return try {
            val user = osuService.getUserFromToken(token, osuId)
            if (user != null && user.hasEditPermissions) {
                service.setBeatmapStatus(osuId, id.toLong(), status)
                true
            } else {
                false
            }
        } catch (ex: Exception) {
            log.error("Error while executing Request", ex)
            false
        }
    }

    @PutMapping("/{id}/update")
    fun updateBeatmap(
            @PathVariable("id") id: String,
            @RequestHeader(name = "Osu-Id") osuId: Long,
            @RequestHeader(name = "Authorization") token: String,
            @RequestBody update: UpdatedBeatmap
    ): Boolean {
        return try {
            val user = osuService.getUserFromToken(token, osuId)
            if (user != null && user.hasEditPermissions) {
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
            @RequestHeader(name = "Osu-Id") osuId: Long,
            @RequestHeader(name = "Authorization") token: String,
            @RequestBody newBeatmap: NewBeatmap
    ): Boolean {
        return try {
            val user = osuService.getUserFromToken(token, osuId)
            if (user != null && user.hasEditPermissions) {
                return service.addBeatmap(
                        editorId = user.osuId,
                        beatmapId = newBeatmap.beatmapId.toLong(),
                        token = token
                )
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
            @RequestParam status: List<Long>?,
            @RequestParam limit: BeatmapFilterLimit,
            @RequestParam page: Int?,
            @RequestParam countTotal: Boolean?,
            @RequestParam hideGraved: Boolean?,
            @RequestParam hideRanked: Boolean?,
            @RequestParam hideWithTwoNominators: Boolean?,
            @RequestParam nominator: List<Long>? = emptyList()
    ): FindResponse<FoundBeatmap> {
        return try {
            return service.findBeatmaps(BeatmapFilter(
                    artist,
                    title,
                    mapper,
                    hideGraved,
                    hideRanked,
                    hideWithTwoNominators,
                    limit,
                    page,
                    countTotal,
                    false,
                    status ?: emptyList(),
                    nominator ?: emptyList()
            ))
        } catch (ex: Exception) {
            log.error("Error while executing Request", ex)
            FindResponse()
        }
    }
}