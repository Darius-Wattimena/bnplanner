package nl.greaper.bnplanner.dataSource

import com.mongodb.client.MongoDatabase
import nl.greaper.bnplanner.model.tournament.ModdingMap
import org.litote.kmongo.findOneById
import org.litote.kmongo.save
import org.springframework.stereotype.Component

@Component
class ModdingMapDataSource(database: MongoDatabase) {
    private val collection = database.getCollection("moddingMap", ModdingMap::class.java)

    fun save(item: ModdingMap) = collection.save(item)

    fun find(id: String) = collection.findOneById(id)

    fun findAll() = collection.find()
}