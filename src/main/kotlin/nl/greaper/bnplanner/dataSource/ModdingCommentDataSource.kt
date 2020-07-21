package nl.greaper.bnplanner.dataSource

import com.mongodb.client.MongoDatabase
import nl.greaper.bnplanner.model.tournament.Contest
import org.litote.kmongo.findOneById
import org.litote.kmongo.save
import org.springframework.stereotype.Component

@Component
class ContestDataSource(database: MongoDatabase) {
    private val collection = database.getCollection("contest", Contest::class.java)

    fun save(contest: Contest) = collection.save(contest)

    fun find(id: String) = collection.findOneById(id)

    fun findAll() = collection.find()
}