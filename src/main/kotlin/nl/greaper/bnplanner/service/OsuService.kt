package nl.greaper.bnplanner.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KotlinLogging
import nl.greaper.bnplanner.OsuHttpClient
import nl.greaper.bnplanner.dataSource.UserDataSource
import nl.greaper.bnplanner.model.auth.UserProfile
import nl.greaper.bnplanner.model.osu.Me
import nl.greaper.bnplanner.model.user.User
import nl.greaper.bnplanner.util.getUserRole
import org.springframework.stereotype.Service

@Service
class OsuService(
        val client: OsuHttpClient,
        val objectMapper: ObjectMapper,
        val userDataSource: UserDataSource
) {
    private val log = KotlinLogging.logger {  }
    private val sessions = mutableMapOf<Long, String>()

    fun getUserInfo(token: String): UserProfile? {
        val response = client.get("/me", token)
        val osuMe = response.body?.let { objectMapper.readValue<Me>(it) }

        if (osuMe != null) {
            return try {
                val user = userDataSource.find(osuMe.id)

                updateUserInfoIfNeeded(user, osuMe)

                sessions[osuMe.id] = token

                UserProfile(osuMe.id, user.hasEditPermissions, user.hasAdminPermissions)
            } catch (ex: Exception) {
                UserProfile(osuMe.id)
            }
        }
        return null
    }

    /**
     * Update the user his info if any changes occurred in the following subjects:
     * - Username
     * - Group
     */
    private fun updateUserInfoIfNeeded(user: User, osuMe: Me) {
        var userChanges = false

        if (user.osuName != osuMe.username) {
            user.osuName = osuMe.username
            user.aliases = osuMe.previous_usernames.toMutableList()
            userChanges = true
        }

        val userRole = getUserRole(user, osuMe.groups)

        if (user.role != userRole) {
            user.role = userRole
            userChanges = true
        }

        if (userChanges) {
            userDataSource.save(user)
        }
    }

    fun getUserFromToken(token: String, osuId: Long): User? {
        return try {
            if (sessions.containsKey(osuId) && sessions[osuId] == token) {
                userDataSource.find(osuId)
            } else {
                val response = client.get("/me", token)
                val osuMe = response.body?.let { objectMapper.readValue<Me>(it) }

                if (osuMe != null) {
                    userDataSource.find(osuMe.id)
                } else {
                    null
                }
            }
        } catch (ex: Exception) {
            log.error(ex) { "Error occurred while trying to get the user from the provided token" }
            null
        }
    }
}