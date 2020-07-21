package nl.greaper.bnplanner.service

import nl.greaper.bnplanner.dataSource.BeatmapDataSource
import nl.greaper.bnplanner.dataSource.ContestDataSource
import nl.greaper.bnplanner.dataSource.UserDataSource
import nl.greaper.bnplanner.exception.BeatmapException
import nl.greaper.bnplanner.model.*
import nl.greaper.bnplanner.model.beatmap.*
import nl.greaper.bnplanner.model.event.Events
import nl.greaper.bnplanner.model.filter.BeatmapFilter
import nl.greaper.bnplanner.model.tournament.Contest
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ContestService(
        val dataSource: ContestDataSource,
) {
    fun find(id: String): Contest? {
        return dataSource.find(id)
    }

    fun save(contest: Contest): Boolean {
        dataSource.save(contest)
        return true
    }
}