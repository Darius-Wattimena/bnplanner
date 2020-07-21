package nl.greaper.bnplanner.service

import nl.greaper.bnplanner.dataSource.ModdingResponseDataSource
import nl.greaper.bnplanner.model.tournament.ModdingResponse
import org.springframework.stereotype.Service

@Service
class ModdingResponseService(
        val dataSource: ModdingResponseDataSource,
) {
    fun find(id: String): ModdingResponse? {
        return dataSource.find(id)
    }

    fun findByModdingComment(id: String): List<ModdingResponse> {
        return dataSource.findByModdingComment(id).toList()
    }

    fun save(item: ModdingResponse): Boolean {
        dataSource.save(item)
        return true
    }
}