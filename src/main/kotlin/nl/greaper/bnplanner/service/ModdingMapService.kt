package nl.greaper.bnplanner.service

import nl.greaper.bnplanner.dataSource.*
import nl.greaper.bnplanner.exception.BeatmapException
import nl.greaper.bnplanner.model.*
import nl.greaper.bnplanner.model.beatmap.*
import nl.greaper.bnplanner.model.event.Events
import nl.greaper.bnplanner.model.filter.BeatmapFilter
import nl.greaper.bnplanner.model.tournament.Contest
import nl.greaper.bnplanner.model.tournament.ModdingCommentWithResponses
import nl.greaper.bnplanner.model.tournament.ModdingDiscussion
import nl.greaper.bnplanner.model.tournament.ModdingMap
import nl.greaper.bnplanner.util.copyableRandomUUID
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ModdingMapService(
        val dataSource: ModdingMapDataSource,
        val moddingCommentService: ModdingCommentService,
        val moddingResponseService: ModdingResponseService
) {
    fun find(id: String): ModdingMap? {
        return dataSource.find(id)
    }

    fun save(item: ModdingMap): Boolean {
        val itemWithId = if (item._id == "") {
            item.copy(_id = copyableRandomUUID())
        } else {
            item
        }
        dataSource.save(itemWithId)
        return true
    }

    fun findMapDiscussion(id: String): ModdingDiscussion? {
        val moddingMap = find(id)

        if (moddingMap != null) {
            val comments = moddingCommentService.findAllByModdingMapId(moddingMap._id)

            val commentWithResponses = comments.map { comment ->
                ModdingCommentWithResponses(comment, moddingResponseService.findByModdingComment(comment._id))
            }

            return ModdingDiscussion(moddingMap, commentWithResponses)
        }

        return null
    }

    fun findAll(): List<ModdingMap> {
        return dataSource.findAll().toList()
    }
}