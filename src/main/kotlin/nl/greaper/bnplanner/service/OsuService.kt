package nl.greaper.bnplanner.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.natpryce.konfig.Configuration
import nl.greaper.bnplanner.OsuHttpClient
import nl.greaper.bnplanner.dataSource.UserDataSource
import nl.greaper.bnplanner.model.auth.UserProfile
import nl.greaper.bnplanner.model.osu.OsuMe
import org.springframework.stereotype.Service

@Service
class OsuService(
        val client: OsuHttpClient,
        val objectMapper: ObjectMapper,
        val userDataSource: UserDataSource
) {
    fun getUserInfo(token: String): UserProfile? {
        val response = client.get("/me", token)
        val osuMe = response.body?.let { objectMapper.readValue<OsuMe>(it) }

        if (osuMe != null) {
            return try {
                val user = userDataSource.find(osuMe.id)
                UserProfile(osuMe.id, user.hasEditPermissions, user.hasAdminPermissions)
            } catch (ex: Exception) {
                UserProfile(osuMe.id)
            }
        }
        return null
    }
}