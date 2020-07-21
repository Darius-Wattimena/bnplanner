package nl.greaper.bnplanner.controller

import mu.KotlinLogging
import nl.greaper.bnplanner.model.tournament.ModdingComment
import nl.greaper.bnplanner.service.ModdingCommentService
import nl.greaper.bnplanner.service.OsuService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/modding/comment")
class ModdingCommentController(
        val service: ModdingCommentService,
        val osuService: OsuService
) {
    private val log = KotlinLogging.logger {}

    @GetMapping("/{id}")
    fun find(
            @RequestHeader(name = "Osu-Id") osuId: Long,
            @RequestHeader(name = "Authorization") token: String,
            @PathVariable("id") id: String
    ): ModdingComment? {
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

    @GetMapping("/findByModdingMapId")
    fun findByModdingMapId(
            @RequestHeader(name = "Osu-Id") osuId: Long,
            @RequestHeader(name = "Authorization") token: String,
            @RequestParam moddingMapId: String
    ): List<ModdingComment> {
        return try {
            val user = osuService.getUserFromToken(token, osuId)
            if (user != null && user.hasHiddenPermissions) {
                service.findAllByModdingMapId(moddingMapId)
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
            @RequestBody item: ModdingComment
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