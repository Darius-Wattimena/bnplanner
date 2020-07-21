package nl.greaper.bnplanner.dataSource

import com.mongodb.client.MongoDatabase
import nl.greaper.bnplanner.model.tournament.Contest
import nl.greaper.bnplanner.model.tournament.ModdingComment
import org.litote.kmongo.findOneById
import org.litote.kmongo.save
import org.springframework.stereotype.Component

@Component
class ModdingCommentDataSource(database: MongoDatabase) {
    private val collection = database.getCollection("moddingComment", ModdingComment::class.java)

    fun save(contest: ModdingComment) = collection.save(contest)

    fun find(id: String) = collection.findOneById(id)

    fun findAll() = collection.find()
}