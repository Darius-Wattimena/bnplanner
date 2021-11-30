package nl.greaper.bnplanner.dataSource

import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Collation
import com.mongodb.client.model.IndexOptions
import nl.greaper.bnplanner.exception.UserException
import nl.greaper.bnplanner.model.LegacyFindResponse
import nl.greaper.bnplanner.model.beatmap.Beatmap
import nl.greaper.bnplanner.model.filter.UserFilter
import nl.greaper.bnplanner.model.user.OsuRole
import nl.greaper.bnplanner.model.user.User
import nl.greaper.bnplanner.util.quote
import org.litote.kmongo.*
import org.springframework.stereotype.Component

@Component
class UserDataSource(database: MongoDatabase) {
    private val collection = database.getCollection("user", User::class.java)

    init {
        collection.ensureIndex(ascending(
                User::osuName,
                User::role
        ), IndexOptions().name("query"))

        collection.ensureIndex(ascending(
                User::lastToken
        ))
    }

    fun findUserWithToken(token: String): User? {
        return collection.findOne(
                User::lastToken eq token
        )
    }

    fun findAiess(): User {
        return try {
            find(-1)
        } catch (ex: Throwable) {
            val aiessUser = User(-1, "Aiess", "", hasAdminPermissions = true, hasEditPermissions = true)
            save(aiessUser)
            aiessUser
        }
    }

    fun findBNPlanner(): User {
        return try {
            find(-2)
        } catch (ex: Throwable) {
            val nominationPlanner = User(-2, "Nomination Planner", "", hasAdminPermissions = true, hasEditPermissions = true)
            save(nominationPlanner)
            nominationPlanner
        }
    }

    fun find(osuId: Long): User = collection.findOne(User::osuId eq osuId)
            ?: throw UserException("Could not find user with provided ID")

    fun findByAuthId(authId: String): User? = collection.findOne(User::authId eq authId)

    fun save(user: User) = collection.save(user)

    fun exists(beatmapId: Long): Boolean {
        return collection.countDocuments(
                Beatmap::osuId eq beatmapId
        ) > 0
    }

    fun findAll(): MutableList<User> {
        return collection.find().toMutableList()
    }

    fun findAll(filter: UserFilter): LegacyFindResponse<User> {
        val query = and(
                and(listOfNotNull(
                        if (filter.name != null) User::osuName regex quote(filter.name).toRegex(RegexOption.IGNORE_CASE) else null,
                        if (filter.canEdit != null) User::hasEditPermissions eq filter.canEdit else null,
                        if (filter.isAdmin != null) User::hasAdminPermissions eq filter.isAdmin else null
                )),
                or(listOfNotNull(
                        if (filter.roles.contains(OsuRole.BN)) User::role eq OsuRole.BN else null,
                        if (filter.roles.contains(OsuRole.PBN)) User::role eq OsuRole.PBN else null,
                        if (filter.roles.contains(OsuRole.CA)) User::role eq OsuRole.CA else null,
                        if (filter.roles.contains(OsuRole.NAT)) User::role eq OsuRole.NAT else null,
                        if (filter.roles.contains(OsuRole.OBS)) User::role eq OsuRole.OBS else null,
                        if (filter.roles.contains(OsuRole.GST)) User::role eq OsuRole.GST else null
                )))

        val findQuery = collection.find(query)
                .collation(Collation.builder().locale("en").build())
                .sort(orderBy(User::osuName))

        if (filter.limit != null) {
            findQuery.limit(filter.limit.asNumber())

            if (filter.page != null && filter.page > 0) {
                findQuery.skip((filter.page - 1) * filter.limit.asNumber())
            }
        } else {
            findQuery.limit(10)
        }

        val totalCount = if (filter.countTotal != null && filter.countTotal) {
            collection.countDocuments(query).toInt()
        } else {
            0
        }

        val result = findQuery.toMutableList()
        return LegacyFindResponse(totalCount, result.count(), result)
    }
}