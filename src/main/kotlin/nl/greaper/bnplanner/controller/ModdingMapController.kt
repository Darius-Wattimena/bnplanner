package nl.greaper.bnplanner.controller

import mu.KotlinLogging
import nl.greaper.bnplanner.model.tournament.Contest
import nl.greaper.bnplanner.model.tournament.ModdingDiscussion
import nl.greaper.bnplanner.model.tournament.ModdingMap
import nl.greaper.bnplanner.service.ModdingMapService
import nl.greaper.bnplanner.service.OsuService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/modding/map")
class ModdingMapController(
        val service: ModdingMapService,
        val osuService: OsuService
) {
    private val log = KotlinLogging.logger {}

    @GetMapping("/{id}")
    fun find(
            @RequestHeader(name = "Osu-Id") osuId: Long,
            @RequestHeader(name = "Authorization") token: String,
            @PathVariable("id") id: String
    ): ModdingMap? {
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
    ): List<ModdingMap> {
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

    @GetMapping("/{id}/findDiscussion")
    fun findDiscussion(
            @RequestHeader(name = "Osu-Id") osuId: Long,
            @RequestHeader(name = "Authorization") token: String,
            @PathVariable("id") id: String
    ): ModdingDiscussion? {
        return try {
            val user = osuService.getUserFromToken(token, osuId)
            if (user != null && user.hasHiddenPermissions) {
                service.findMapDiscussion(id)
            } else {
                null
            }
        } catch (ex: Exception) {
            log.error("Error while executing Request", ex)
            null
        }
    }

    @PostMapping("/add")
    fun add(
            @RequestHeader(name = "Osu-Id") osuId: Long,
            @RequestHeader(name = "Authorization") token: String,
            @RequestBody item: ModdingMap
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

    @PutMapping("/update")
    fun update(
            @RequestHeader(name = "Osu-Id") osuId: Long,
            @RequestHeader(name = "Authorization") token: String,
            @RequestBody item: ModdingMap
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