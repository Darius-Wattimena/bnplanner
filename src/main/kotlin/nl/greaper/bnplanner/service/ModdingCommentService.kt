package nl.greaper.bnplanner.service

import nl.greaper.bnplanner.dataSource.BeatmapDataSource
import nl.greaper.bnplanner.dataSource.ContestDataSource
import nl.greaper.bnplanner.dataSource.ModdingMapDataSource
import nl.greaper.bnplanner.dataSource.UserDataSource
import nl.greaper.bnplanner.exception.BeatmapException
import nl.greaper.bnplanner.model.*
import nl.greaper.bnplanner.model.beatmap.*
import nl.greaper.bnplanner.model.event.Events
import nl.greaper.bnplanner.model.filter.BeatmapFilter
import nl.greaper.bnplanner.model.tournament.Contest
import nl.greaper.bnplanner.model.tournament.ModdingMap
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ModdingMapService(
        val dataSource: ModdingMapDataSource,
) {
    fun find(id: String): ModdingMap? {
        return dataSource.find(id)
    }

    fun save(item: ModdingMap): Boolean {
        dataSource.save(item)
        return true
    }
}