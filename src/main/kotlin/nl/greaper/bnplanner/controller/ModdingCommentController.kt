package nl.greaper.bnplanner.controller

import mu.KotlinLogging
import nl.greaper.bnplanner.model.tournament.ModdingResponse
import nl.greaper.bnplanner.service.ModdingResponseService
import nl.greaper.bnplanner.service.OsuService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/modding/response")
class ModdingResponseController(
        val service: ModdingResponseService,
        val osuService: OsuService
) {
    private val log = KotlinLogging.logger {}

    @GetMapping("/{id}")
    fun findContest(
            @RequestHeader(name = "Osu-Id") osuId: Long,
            @RequestHeader(name = "Authorization") token: String,
            @PathVariable("id") id: String
    ): ModdingResponse? {
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
            @RequestBody item: ModdingResponse
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