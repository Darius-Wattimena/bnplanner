package nl.greaper.bnplanner.service

import nl.greaper.bnplanner.dataSource.UserDataSource
import nl.greaper.bnplanner.exception.UserException
import nl.greaper.bnplanner.model.FindResponse
import nl.greaper.bnplanner.model.event.DetailedEvent
import nl.greaper.bnplanner.model.event.Events
import nl.greaper.bnplanner.model.filter.UserFilter
import nl.greaper.bnplanner.model.user.*
import org.springframework.stereotype.Service

@Service
class UserService(
        val dataSource: UserDataSource,
        val osuService: OsuService
) {
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
                user.role,
                user.events
        )
    }

    fun findUser(osuId: Long): User {
        return dataSource.find(osuId)
    }

    fun findUsers(): List<User> {
        return dataSource.findAll().sortedWith(compareBy({it.role}, {it.osuName}))
    }

    fun findUsers(userFilter: UserFilter): FindResponse<FoundUser> {
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

        return FindResponse(
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
            
            user.events.add(Events.asUserUpdateUsernameEvent(editorId, user.osuName, updated.osuName))
            user.osuName = updated.osuName
        }

        if (user.role != updated.role) {
            user.role = updated.role
            user.events.add(Events.asUserUpdateRoleEvent(editorId, updated.role))
        }

        if (user.hasAdminPermissions != updated.hasAdminPermissions) {
            val event = if (updated.hasAdminPermissions) {
                Events.asUserAddAdminPermissionEvent(editorId)
            } else {
                Events.asUserRemoveAdminPermissionEvent(editorId)
            }
            user.events.add(event)
            user.hasAdminPermissions = updated.hasAdminPermissions
        }

        if (user.hasEditPermissions != updated.hasEditPermissions) {
            val event = if (updated.hasEditPermissions) {
                Events.asUserAddEditPermissionEvent(editorId)
            } else {
                Events.asUserRemoveEditPermissionEvent(editorId)
            }
            user.events.add(event)
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
            user.events.add(Events.asUserCreatedEvent(editorId))
            dataSource.save(user)
        }
    }
}