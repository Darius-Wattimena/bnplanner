package nl.greaper.bnplanner.dataSource

import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.IndexOptions
import nl.greaper.bnplanner.model.UserStatistics
import nl.greaper.bnplanner.model.UserStatistics.StatisticsType
import org.litote.kmongo.*
import org.springframework.stereotype.Component

@Component
class UserStatisticsDataSource(database: MongoDatabase) {
    private val collection = database.getCollection("userStatistic", UserStatistics::class.java)

    init {
        collection.ensureIndex(ascending(
                UserStatistics::osuId,
                UserStatistics::timestamp
        ), IndexOptions().name("query"))
    }

    fun save(item: UserStatistics) = collection.save(item)

    fun saveAll(items: List<UserStatistics>) = items.forEach {
        val databaseStatistics = find(it.osuId, it.timestamp, it.statisticsType)

        if (databaseStatistics != null) {
            save(it.copy(_id = databaseStatistics._id))
        } else {
            save(it)
        }
    }

    fun find(osuId: Long, timestamp: Long, type: StatisticsType) = collection.findOne(and(
            UserStatistics::osuId eq osuId,
            UserStatistics::timestamp eq timestamp,
            UserStatistics::statisticsType eq type
    ))

    fun findAllForUser(osuId: Long, start: Long, end: Long, type: StatisticsType): List<UserStatistics> {
        val findQuery = collection.find(and(
                UserStatistics::osuId eq osuId,
                UserStatistics::timestamp gte start,
                UserStatistics::timestamp lte end,
                UserStatistics::statisticsType eq type
        ))

        return findQuery.toMutableList()
    }

    fun delete(id: String) = collection.deleteOneById(id).wasAcknowledged()
}