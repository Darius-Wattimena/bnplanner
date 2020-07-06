package nl.greaper.bnplanner.controller

import nl.greaper.bnplanner.model.FindResponse
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
    @GetMapping("/withAuth/{authId}")
    fun findUserWithAuth(@PathVariable("authId") authId: String): User? {
        return try {
            service.findUserWithAuth(authId)
        } catch (ex: Exception) {
            null
        }
    }

    @GetMapping("/{id}")
    fun findUser(@PathVariable("id") osuId: String): User? {
        return try {
            service.findUser(osuId.toLong())
        } catch (ex: Exception) {
            null
        }
    }

    @GetMapping("/{id}/detailed")
    fun findDetailedUser(@PathVariable("id") osuId: String): DetailedUser? {
        return try {
            service.findDetailedUser(osuId.toLong())
        } catch (ex: Exception) {
            null
        }
    }

    @GetMapping("/findAll")
    fun findAllUsers(): List<User> {
        return try {
            return service.findUsers()
        } catch (ex: Exception) {
            emptyList()
        }
    }


    @GetMapping("/searchByFilter")
    fun findUsers(
            @RequestParam name: String?,
            @RequestParam roles: List<OsuRole>?,
            @RequestParam limit: Int?,
            @RequestParam page: Int?,
            @RequestParam countTotal: Boolean?
    ): FindResponse<FoundUser> {
        return try {
            return service.findUsers(name, roles, limit, page, countTotal)
        } catch (ex: Exception) {
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
            false
        }
    }
}