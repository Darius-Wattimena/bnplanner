package nl.greaper.bnplanner.controller

import mu.KotlinLogging
import nl.greaper.bnplanner.model.FindResponse
import nl.greaper.bnplanner.model.filter.UserFilter
import nl.greaper.bnplanner.model.filter.UserFilterLimit
import nl.greaper.bnplanner.model.user.*
import nl.greaper.bnplanner.service.OsuService
import nl.greaper.bnplanner.service.UserService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/user")
class UserController(
        val service: UserService,
        val osuService: OsuService
) {
    private val log = KotlinLogging.logger {}

    @GetMapping("/withAuth/{authId}")
    fun findUserWithAuth(@PathVariable("authId") authId: String): User? {
        return try {
            service.findUserWithAuth(authId)
        } catch (ex: Exception) {
            log.error("Error while executing Request", ex)
            null
        }
    }

    @GetMapping("/{id}")
    fun findUser(@PathVariable("id") osuId: String): FoundUser? {
        return try {
            service.findUser(osuId.toLong())
        } catch (ex: Exception) {
            log.error("Error while executing Request", ex)
            null
        }
    }

    @GetMapping("/{id}/detailed")
    fun findDetailedUser(@PathVariable("id") osuId: String): DetailedUser? {
        return try {
            service.findDetailedUser(osuId.toLong())
        } catch (ex: Exception) {
            log.error("Error while executing Request", ex)
            null
        }
    }

    @GetMapping("/findAll")
    fun findAllUsers(): List<FoundUser> {
        return try {
            return service.findUsers()
        } catch (ex: Exception) {
            log.error("Error while executing Request", ex)
            emptyList()
        }
    }

    @GetMapping("/searchByFilter")
    fun findUsers(
            @RequestParam name: String?,
            @RequestParam roles: List<OsuRole>?,
            @RequestParam limit: UserFilterLimit?,
            @RequestParam page: Int?,
            @RequestParam countTotal: Boolean?,
            @RequestParam canEdit: Boolean?,
            @RequestParam isAdmin: Boolean?
    ): FindResponse<FoundUser> {
        return try {
            return service.findUsers(UserFilter(
                    name,
                    canEdit,
                    isAdmin,
                    limit,
                    page,
                    countTotal,
                    roles ?: emptyList()
            ))
        } catch (ex: Exception) {
            log.error("Error while executing Request", ex)
            FindResponse()
        }
    }

    @PostMapping("/add")
    fun addUser(
            @RequestHeader(name = "Osu-Id") osuId: Long,
            @RequestHeader(name = "Authorization") token: String,
            @RequestBody newUser: NewUser
    ): Boolean {
        return try {
            val user = osuService.getUserFromToken(token, osuId)
            if (user != null && user.hasAdminPermissions) {
                service.addUser(user.osuId, newUser, token)
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
    fun updateUser(
            @PathVariable("id") id: String,
            @RequestHeader(name = "Osu-Id") osuId: Long,
            @RequestHeader(name = "Authorization") token: String,
            @RequestBody update: UpdatedUser
    ): Boolean {
        return try {
            val user = osuService.getUserFromToken(token, osuId)
            if (user != null && user.hasAdminPermissions) {
                service.updateUser(user.osuId, id.toLong(), update)
                true
            } else {
                false
            }
        } catch (ex: Exception) {
            log.error("Error while executing Request", ex)
            false
        }
    }
}