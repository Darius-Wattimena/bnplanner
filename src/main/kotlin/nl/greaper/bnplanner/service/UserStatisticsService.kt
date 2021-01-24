package nl.greaper.bnplanner.service

import com.mongodb.client.FindIterable
import nl.greaper.bnplanner.dataSource.StatisticsDataSource
import nl.greaper.bnplanner.dataSource.UserStatisticsDataSource
import nl.greaper.bnplanner.model.Statistics
import nl.greaper.bnplanner.model.UserStatistics
import org.springframework.stereotype.Service

@Service
class UserStatisticsService(val dataSource: UserStatisticsDataSource) {
    fun find(osuId: Long, start: Long, end: Long, type: UserStatistics.StatisticsType): List<UserStatistics> {
        return dataSource.findAllForUser(osuId, start, end, type)
    }
}