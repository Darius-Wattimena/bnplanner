package nl.greaper.bnplanner.dataSource

import com.mongodb.client.MongoDatabase
import nl.greaper.bnplanner.model.tournament.ModdingResponse
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.eq
import org.litote.kmongo.findOneById
import org.litote.kmongo.save
import org.springframework.stereotype.Component

@Component
class ModdingResponseDataSource(database: MongoDatabase) {
    private val collection = database.getCollection("moddingResponse", ModdingResponse::class.java)

    fun save(item: ModdingResponse) = collection.save(item)

    fun find(id: String) = collection.findOneById(id)

    fun findByModdingComment(id: String) = collection.find(ModdingResponse::moddingCommentId eq id)

    fun findAll() = collection.find()

    fun delete(id: String) = collection.deleteOneById(id).wasAcknowledged()

    fun deleteByCommentId(commentId: String): Boolean {
        return collection.deleteMany(ModdingResponse::moddingCommentId eq commentId).wasAcknowledged()
    }
}