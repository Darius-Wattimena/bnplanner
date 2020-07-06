package nl.greaper.bnplanner.dataSource

import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Collation
import com.mongodb.client.model.IndexOptions
import nl.greaper.bnplanner.exception.UserException
import nl.greaper.bnplanner.model.FindResponse
import nl.greaper.bnplanner.model.beatmap.Beatmap
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

    fun findAll(name: String?, roles: List<OsuRole>, limit: Int?, page: Int?, countTotal: Boolean?): FindResponse<User> {
        val query = and(
                and(listOfNotNull(
                        if (name != null) { User::osuName regex quote(name).toRegex(RegexOption.IGNORE_CASE) } else null
                )),
                or(listOfNotNull(
                        if (roles.contains(OsuRole.OBS)) User::role eq OsuRole.OBS else null,
                        if (roles.contains(OsuRole.BN)) User::role eq OsuRole.BN else null,
                        if (roles.contains(OsuRole.PBN)) User::role eq OsuRole.PBN else null,
                        if (roles.contains(OsuRole.CA)) User::role eq OsuRole.CA else null,
                        if (roles.contains(OsuRole.NAT)) User::role eq OsuRole.NAT else null
                )))

        val findQuery = collection.find(query)
                .collation(Collation.builder().locale("en").build())
                .sort(orderBy(User::osuName))

        if (limit != null) {
            findQuery.limit(limit)
        } else {
            findQuery.limit(10)
        }

        if (page != null && limit != null && page > 0) {
            findQuery.skip((page - 1) * limit)
        }

        val totalCount = if (countTotal != null && countTotal) {
            collection.countDocuments(query).toInt()
        } else {
            0
        }

        val result = findQuery.toMutableList()
        return FindResponse(totalCount, result.count(), result)
    }
}