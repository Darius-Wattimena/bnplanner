package nl.greaper.bnplanner.service

import nl.greaper.bnplanner.DiscordWebhookClient
import nl.greaper.bnplanner.dataSource.UserDataSource
import nl.greaper.bnplanner.exception.UserException
import nl.greaper.bnplanner.model.LegacyFindResponse
import nl.greaper.bnplanner.model.discord.EmbedColor
import nl.greaper.bnplanner.model.discord.EmbedFooter
import nl.greaper.bnplanner.model.discord.EmbedThumbnail
import nl.greaper.bnplanner.model.event.Events
import nl.greaper.bnplanner.model.filter.UserFilter
import nl.greaper.bnplanner.model.user.*
import org.springframework.stereotype.Service

@Service
class UserService(
        val dataSource: UserDataSource,
        val osuService: OsuService,
        val discordWebhookClient: DiscordWebhookClient
) {
    companion object {
        const val CREATED_USER = "\uD83C\uDF1F" // 🌟
    }

    fun findUserWithAuth(authId: String): User? {
        return dataSource.findByAuthId(authId)
    }

    fun findDetailedUser(osuId: Long): DetailedUser {
        val user = dataSource.find(osuId)

        return DetailedUser(
                user.osuId,
                user.osuName,
                user.profilePictureUri,
                user.aliases,
                user.hasEditPermissions,
                user.hasAdminPermissions,
                user.hasHiddenPermissions,
                user.role,
                user.plannerEvents
        )
    }

    fun findUser(osuId: Long): FoundUser {
        val user = dataSource.find(osuId)
        return FoundUser(user.osuId, user.osuName, user.aliases, user.profilePictureUri, user.hasEditPermissions, user.hasAdminPermissions, user.role)
    }

    fun findUsers(): List<FoundUser> {
        return dataSource.findAll().sortedWith(compareBy({it.role}, {it.osuName})).map {
            FoundUser(it.osuId, it.osuName, it.aliases, it.profilePictureUri, it.hasEditPermissions, it.hasAdminPermissions, it.role)
        }
    }

    fun findUsers(userFilter: UserFilter): LegacyFindResponse<FoundUser> {
        val foundUsers = dataSource.findAll(userFilter)

        val result = foundUsers.response.map {user ->
            FoundUser(
                    user.osuId,
                    user.osuName,
                    user.aliases,
                    user.profilePictureUri,
                    user.hasEditPermissions,
                    user.hasAdminPermissions,
                    user.role
            )
        }

        return LegacyFindResponse(
                foundUsers.total,
                foundUsers.count,
                result,
                foundUsers.uuid
        )
    }

    fun updateUser(editorId: Long, osuId: Long, updated: UpdatedUser) {
        val user = dataSource.find(osuId)

        if (user.osuName != updated.osuName) {
            // Add old name to aliases
            if (!user.aliases.contains(user.osuName)) {
                user.aliases.add(user.osuName)
            }

            //User reverted back to one of their old names
            if (user.aliases.contains(updated.osuName)) {
                user.aliases.remove(updated.osuName)
            }
            
            user.plannerEvents.add(Events.asUserUpdateUsernameEvent(editorId, user.osuName, updated.osuName))
            user.osuName = updated.osuName
        }

        if (user.role != updated.role) {
            user.role = updated.role
            user.plannerEvents.add(Events.asUserUpdateRoleEvent(editorId, updated.role))
        }

        if (user.hasAdminPermissions != updated.hasAdminPermissions) {
            val event = if (updated.hasAdminPermissions) {
                Events.asUserAddAdminPermissionEvent(editorId)
            } else {
                Events.asUserRemoveAdminPermissionEvent(editorId)
            }
            user.plannerEvents.add(event)
            user.hasAdminPermissions = updated.hasAdminPermissions
        }

        if (user.hasEditPermissions != updated.hasEditPermissions) {
            val event = if (updated.hasEditPermissions) {
                Events.asUserAddEditPermissionEvent(editorId)
            } else {
                Events.asUserRemoveEditPermissionEvent(editorId)
            }
            user.plannerEvents.add(event)
            user.hasEditPermissions = updated.hasEditPermissions
        }

        dataSource.save(user)
    }

    fun addUser(editorId: Long, newUser: NewUser, token: String) {
        if (dataSource.exists(newUser.osuId)) {
            throw UserException("User with the provided id already registered on the planner")
        }
        val foundUser = osuService.findUserWithId(token, newUser.osuId)
        if (foundUser != null) {
            val user = User(
                    foundUser.id,
                    foundUser.username,
                    "https://a.ppy.sh/${newUser.osuId}",
                    foundUser.previous_usernames.toMutableList()
            )
            dataSource.save(user)
            user.plannerEvents.add(Events.asUserCreatedEvent(editorId))
            dataSource.save(user)

            val bnPlannerUser = dataSource.findBNPlanner()

            discordWebhookClient.send(
                """$CREATED_USER **Created**
                    **[${user.osuName}](https://osu.ppy.sh/users/${user.osuId})** has been added to the Planner!
                """.prependIndent(),
                EmbedColor.GOLD,
                EmbedThumbnail(user.profilePictureUri),
                EmbedFooter(bnPlannerUser.osuName, bnPlannerUser.profilePictureUri),
                confidential = true
            )
        }
    }
}