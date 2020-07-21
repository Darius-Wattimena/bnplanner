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
    fun findContest(
            @RequestHeader(name = "Osu-Id") osuId: Long,
            @RequestHeader(name = "Authorization") token: String,
            @PathVariable("id") id: String
    ): Contest? {
        return try {
            service.find(id)
        } catch (ex: Exception) {
            log.error("Error while executing Request", ex)
            null
        }
    }

    @PostMapping("/add")
    fun addContest(
            @RequestHeader(name = "Osu-Id") osuId: Long,
            @RequestHeader(name = "Authorization") token: String,
            @RequestBody contest: Contest
    ): Boolean {
        return try {
            val user = osuService.getUserFromToken(token, osuId)
            if (user != null && user.hasHiddenPermissions) {
                return service.save(contest)
            } else {
                false
            }
        } catch (ex: Exception) {
            log.error("Error while executing Request", ex)
            false
        }
    }
}