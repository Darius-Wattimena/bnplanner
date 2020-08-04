package nl.greaper.bnplanner.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KotlinLogging
import nl.greaper.bnplanner.OsuHttpClient
import nl.greaper.bnplanner.dataSource.UserDataSource
import nl.greaper.bnplanner.model.auth.UserProfile
import nl.greaper.bnplanner.model.osu.BeatmapSet
import nl.greaper.bnplanner.model.osu.Me
import nl.greaper.bnplanner.model.user.User
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class OsuService(
        val client: OsuHttpClient,
        val objectMapper: ObjectMapper,
        val userDataSource: UserDataSource
) {
    private val log = KotlinLogging.logger {  }

    fun getUserInfo(token: String): UserProfile? {
        val response = client.get("/me", token)
        val osuMe = response.body?.let { objectMapper.readValue<Me>(it) }

        if (osuMe != null) {

            // user doesn't exists so we create a new one
            val potentionNewUser = try {
                userDataSource.find(osuMe.id)
            } catch (ex: Exception) {
                null
            } ?: User(
                    osuMe.id,
                    osuMe.username,
                    "https://a.ppy.sh/${osuMe.id}",
                    osuMe.previous_usernames.toMutableList()
            )

            return try{
                val user = updateUserInfoIfNeeded(
                        potentionNewUser,
                        osuMe,
                        token
                )

                UserProfile(osuMe.id, user.hasEditPermissions, user.hasAdminPermissions, user.hasHiddenPermissions)
            } catch (ex: Exception) {
                UserProfile(osuMe.id)
            }
        }
        return null
    }

    /**
     * Update the user his info if any changes occurred in the following subjects:
     * - Username
     */
    private fun updateUserInfoIfNeeded(user: User, osuMe: Me, token: String): User {
        if (user.osuName != osuMe.username) {
            user.osuName = osuMe.username
            user.aliases = osuMe.previous_usernames.toMutableList()
        }

        user.lastToken = token
        user.lastTokenExpire = Instant.now().epochSecond + 86400 // is 24 hours default expire date right now

        userDataSource.save(user)

        return user
    }

    fun findUserWithId(token: String, osuId: Long): Me? {
        return try {
            val response = client.get("/users/$osuId", token)
            return response.body?.let { objectMapper.readValue<Me>(it) }
        } catch (ex: Exception) {
            log.error(ex) { "Error occurred while trying to get user $osuId from the osu api" }
            null
        }
    }

    fun findBeatmapSetInfo(token: String, beatmapSetId: Long): BeatmapSet? {
        return try {
            val response = client.get("/beatmapsets/$beatmapSetId", token)
            return response.body?.let { objectMapper.readValue<BeatmapSet>(it) }
        } catch (ex: Exception) {
            log.error(ex) { "Error occurred while trying to get the beatmap set from the osu api" }
            null
        }
    }

    fun getUserFromToken(token: String, osuId: Long): User? {
        try {
            val foundUser = userDataSource.findUserWithToken(token)

            if (foundUser != null) {
                val expireDate = Instant.ofEpochSecond(foundUser.lastTokenExpire)

                if (expireDate.isAfter(Instant.now())) {
                    return foundUser
                } else {
                    val response = client.get("/me", token)
                    val osuMe = response.body?.let { objectMapper.readValue<Me>(it) }

                    if (osuMe != null) {
                        return updateUserInfoIfNeeded(foundUser, osuMe, token)
                    }
                }
            }
        } catch (ex: Exception) {
            log.error(ex) { "Error occurred while trying to get user $osuId from the osu api" }
        }

        return null
    }
}