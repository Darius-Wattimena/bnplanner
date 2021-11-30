package nl.greaper.bnplanner.dataSource

import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.IndexOptions
import nl.greaper.bnplanner.model.Statistics
import org.litote.kmongo.*
import org.springframework.stereotype.Component

@Component
class StatisticsDataSource(database: MongoDatabase) {
    private val collection = database.getCollection("statistic", Statistics::class.java)

    init {
        collection.ensureIndex(ascending(
                Statistics::timestamp
        ), IndexOptions().name("query"))
    }

    fun insert(item: Statistics) = collection.insertOne(item)

    fun find(id: String) = collection.findOneById(id)

    fun findLatest() = collection.find()
            .sort(descending(Statistics::timestamp))
            .limit(1)
            .firstOrNull()

    fun delete(id: String) = collection.deleteOneById(id).wasAcknowledged()
}