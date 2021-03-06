package nl.greaper.bnplanner.controller

import mu.KotlinLogging
import nl.greaper.bnplanner.model.beatmap.NewBeatmap
import nl.greaper.bnplanner.model.tournament.Contest
import nl.greaper.bnplanner.service.ContestService
import nl.greaper.bnplanner.service.OsuService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/contest")
class ContestController(
        val service: ContestService,
        val osuService: OsuService
) {
    private val log = KotlinLogging.logger {}

    @GetMapping("/{id}")
    fun find(
            @RequestHeader(name = "Osu-Id") osuId: Long,
            @RequestHeader(name = "Authorization") token: String,
            @PathVariable("id") id: String
    ): Contest? {
        return try {
            val user = osuService.getUserFromToken(token, osuId)
            if (user != null && user.hasHiddenPermissions) {
                service.find(id)
            } else {
                null
            }
        } catch (ex: Exception) {
            log.error("Error while executing Request", ex)
            null
        }
    }

    @GetMapping("/findAll")
    fun findAll(
            @RequestHeader(name = "Osu-Id") osuId: Long,
            @RequestHeader(name = "Authorization") token: String
    ): List<Contest> {
        return try {
            val user = osuService.getUserFromToken(token, osuId)
            if (user != null && user.hasHiddenPermissions) {
                service.findAll()
            } else {
                emptyList()
            }
        } catch (ex: Exception) {
            log.error("Error while executing Request", ex)
            emptyList()
        }
    }

    @PostMapping("/add")
    fun add(
            @RequestHeader(name = "Osu-Id") osuId: Long,
            @RequestHeader(name = "Authorization") token: String,
            @RequestBody item: Contest
    ): Boolean {
        return try {
            val user = osuService.getUserFromToken(token, osuId)
            if (user != null && user.hasHiddenPermissions) {
                return service.save(item)
            } else {
                false
            }
        } catch (ex: Exception) {
            log.error("Error while executing Request", ex)
            false
        }
    }
}