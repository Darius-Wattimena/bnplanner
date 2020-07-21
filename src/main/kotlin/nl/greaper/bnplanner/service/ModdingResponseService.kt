package nl.greaper.bnplanner.service

import nl.greaper.bnplanner.dataSource.*
import nl.greaper.bnplanner.exception.BeatmapException
import nl.greaper.bnplanner.model.*
import nl.greaper.bnplanner.model.beatmap.*
import nl.greaper.bnplanner.model.event.Events
import nl.greaper.bnplanner.model.filter.BeatmapFilter
import nl.greaper.bnplanner.model.tournament.Contest
import nl.greaper.bnplanner.model.tournament.ModdingComment
import nl.greaper.bnplanner.model.tournament.ModdingMap
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ModdingCommentService(
        val dataSource: ModdingCommentDataSource,
) {
    fun find(id: String): ModdingComment? {
        return dataSource.find(id)
    }

    fun save(item: ModdingComment): Boolean {
        dataSource.save(item)
        return true
    }
}