package nl.greaper.bnplanner.controller

import com.natpryce.konfig.Configuration
import mu.KotlinLogging
import nl.greaper.bnplanner.config.KonfigConfiguration
import nl.greaper.bnplanner.config.KonfigConfiguration.aiess
import nl.greaper.bnplanner.model.FindResponse
import nl.greaper.bnplanner.model.beatmap.*
import nl.greaper.bnplanner.model.event.AiessBeatmapEvent
import nl.greaper.bnplanner.model.filter.BeatmapFilter
import nl.greaper.bnplanner.model.filter.BeatmapFilterLimit
import nl.greaper.bnplanner.service.BeatmapService
import nl.greaper.bnplanner.service.OsuService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/beatmap")
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

    @GetMapping("/{id}/detailed")
    fun findDetailedBeatmap(@PathVariable("id") id: String): DetailedBeatmap? {
        return try {
            service.findDetailedBeatmap(id.toLong())
        } catch (ex: Exception) {
            log.error("Error while executing Request", ex)
            null
        }
    }

    @GetMapping("/{id}/refreshMetadata")
    fun refreshMetadata(
            @PathVariable("id") id: String,
            @RequestHeader(name = "Osu-Id") osuId: Long,
            @RequestHeader(name = "Authorization") token: String
    ): Boolean {
        return try {
            val user = osuService.getUserFromToken(token, osuId)
            if (user != null && user.hasEditPermissions) {
                service.refreshMetadata(user, id.toLong(), token)
                true
            } else {
                false
            }
        } catch (ex: Exception) {
            log.error("Error while executing Request", ex)
            false
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
                service.deleteBeatmap(id.toLong(), user)
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
                service.setBeatmapStatus(user, id.toLong(), status)
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
                service.updateBeatmap(user, id.toLong(), update)
                true
            } else {
                false
            }
        } catch (ex: Exception) {
            log.error("Error while executing Request", ex)
            false
        }
    }

    @PostMapping("/add/event/aiess")
    fun addAiessEvent(
        @RequestHeader(name = "Authorization") token: String,
        @RequestBody aiessEvent: AiessBeatmapEvent
    ): ResponseEntity<Boolean?> {
        return if (config[aiess.enabled]) {
            if ("Bearer " + config[aiess.token] == token) {
                log.info("Received aiess event")
                service.addAiessEventToBeatmap(aiessEvent)
                ResponseEntity(HttpStatus.OK)
            } else {
                log.info("Received aiess event, incorrect token")
                ResponseEntity(HttpStatus.UNAUTHORIZED)
            }

        } else {
            log.info("Received aiess event, aiess is not enabled in the config!")
            ResponseEntity(HttpStatus.OK)
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
                        editor = user,
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
                    null,
                    hideGraved,
                    hideRanked,
                    hideWithTwoNominators,
                    limit,
                    page,
                    countTotal,
                    false,
                    null,
                    null,
                    status ?: emptyList(),
                    nominator ?: emptyList()
            ))
        } catch (ex: Exception) {
            log.error("Error while executing Request", ex)
            FindResponse()
        }
    }
}