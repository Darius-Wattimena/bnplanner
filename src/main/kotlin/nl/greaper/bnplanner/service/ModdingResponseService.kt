package nl.greaper.bnplanner.service

import nl.greaper.bnplanner.dataSource.ModdingResponseDataSource
import nl.greaper.bnplanner.model.tournament.ModdingResponse
import nl.greaper.bnplanner.util.copyableRandomUUID
import org.springframework.stereotype.Service

@Service
class ModdingResponseService(
        val dataSource: ModdingResponseDataSource
) {
    fun find(id: String): ModdingResponse? {
        return dataSource.find(id)
    }

    fun findByModdingComment(id: String): List<ModdingResponse> {
        return dataSource.findByModdingComment(id).toList()
    }

    fun save(item: ModdingResponse): Boolean {
        val itemWithId = if (item._id == "") {
            item.copy(_id = copyableRandomUUID())
        } else {
            item
        }
        dataSource.save(itemWithId)
        return true
    }

    fun delete(id: String): Boolean {
        return dataSource.delete(id)
    }

    fun deleteByCommentId(commentId: String): Boolean {
        return dataSource.deleteByCommentId(commentId)
    }
}