package nl.greaper.bnplanner.dataSource

import com.mongodb.client.MongoDatabase
import nl.greaper.bnplanner.model.tournament.ModdingComment
import org.litote.kmongo.eq
import org.litote.kmongo.findOneById
import org.litote.kmongo.save
import org.springframework.stereotype.Component

@Component
class ModdingCommentDataSource(database: MongoDatabase) {
    private val collection = database.getCollection("moddingComment", ModdingComment::class.java)

    fun save(item: ModdingComment) = collection.save(item)

    fun find(id: String) = collection.findOneById(id)

    fun findAllByModdingMap(moddingMapId: String) = collection.find(ModdingComment::moddingMapId eq moddingMapId)

    fun findAll() = collection.find()
}