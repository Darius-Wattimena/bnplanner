package nl.greaper.bnplanner.service

import nl.greaper.bnplanner.dataSource.ModdingCommentDataSource
import nl.greaper.bnplanner.model.tournament.ModdingComment
import nl.greaper.bnplanner.model.tournament.ModdingMap
import nl.greaper.bnplanner.util.copyableRandomUUID
import org.springframework.stereotype.Service

@Service
class ModdingCommentService(
        val dataSource: ModdingCommentDataSource,
) {
    fun find(id: String): ModdingComment? {
        return dataSource.find(id)
    }

    fun findAllByModdingMapId(id: String): List<ModdingComment> {
        return dataSource.findAllByModdingMap(id).toList()
    }

    fun save(item: ModdingComment): Boolean {
        val itemWithId = if (item._id == "") {
            item.copy(_id = copyableRandomUUID())
        } else {
            item
        }
        dataSource.save(itemWithId)
        return true
    }
}