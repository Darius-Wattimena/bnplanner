package nl.greaper.bnplanner.service

import nl.greaper.bnplanner.dataSource.StatisticsDataSource
import nl.greaper.bnplanner.model.Statistics
import org.springframework.stereotype.Service

@Service
class StatisticsService(val dataSource: StatisticsDataSource) {
    fun findLatest(): Statistics? {
        return dataSource.findLatest()
    }
}