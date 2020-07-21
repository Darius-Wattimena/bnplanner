package nl.greaper.bnplanner.service

import nl.greaper.bnplanner.dataSource.ContestDataSource
import nl.greaper.bnplanner.model.tournament.Contest
import org.springframework.stereotype.Service

@Service
class ContestService(
        val dataSource: ContestDataSource,
) {
    fun find(id: String): Contest? {
        return dataSource.find(id)
    }

    fun findAll(): List<Contest> {
        return dataSource.findAll().toList()
    }

    fun save(item: Contest): Boolean {
        dataSource.save(item)
        return true
    }
}