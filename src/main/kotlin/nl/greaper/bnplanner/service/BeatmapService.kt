package nl.greaper.bnplanner.service

import nl.greaper.bnplanner.dataSource.BeatmapDataSource
import nl.greaper.bnplanner.dataSource.UserDataSource
import nl.greaper.bnplanner.exception.BeatmapException
import nl.greaper.bnplanner.model.*
import nl.greaper.bnplanner.model.beatmap.*
import nl.greaper.bnplanner.model.event.DetailedEvent
import nl.greaper.bnplanner.model.event.Events
import nl.greaper.bnplanner.model.filter.BeatmapFilter
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class BeatmapService(
        val dataSource: BeatmapDataSource,
        val userDataSource: UserDataSource,
        val osuService: OsuService
) {
    fun addBeatmap(editorId: Long, beatmapId: Long, token: String): Boolean {
        if (dataSource.exists(beatmapId)) {
            throw BeatmapException("Beatmapset already registered on the planner")
        }
        val now = Instant.now().epochSecond
        val beatmapSet = osuService.findBeatmapSetInfo(token, beatmapId)
        return if (beatmapSet != null) {
            val newBeatmap = Beatmap(beatmapId, beatmapSet.artist, beatmapSet.title, "", beatmapSet.creator, dateAdded = now, dateUpdated = now)
            dataSource.save(newBeatmap)
            newBeatmap.events.add(Events.asBeatmapCreatedEvent(editorId))
            dataSource.save(newBeatmap)
            true
        } else {
            false
        }
    }

    fun findBeatmap(beatmapId: Long): Beatmap {
        return dataSource.find(beatmapId)
    }

    fun findDetailedBeatmap(beatmapId: Long): DetailedBeatmap {
        val beatmap = dataSource.find(beatmapId)

        return DetailedBeatmap(
                beatmap.osuId,
                beatmap.artist,
                beatmap.title,
                beatmap.note,
                beatmap.mapper,
                beatmap.status,
                beatmap.nominators,
                beatmap.interested,
                beatmap.events.map { event -> DetailedEvent(userDataSource.find(event.userId), event.title, event.description, event.timestamp) }
        )
    }

    fun findBeatmaps(filter: BeatmapFilter): FindResponse<FoundBeatmap> {
        val foundBeatmaps = dataSource.findAll(filter)
        val result = foundBeatmaps.response.map {beatmap ->
            FoundBeatmap(
                    beatmap.osuId,
                    beatmap.artist,
                    beatmap.title,
                    beatmap.note,
                    beatmap.mapper,
                    beatmap.status,
                    beatmap.nominators.mapNotNull { osuId -> if (osuId != 0L) userDataSource.find(osuId) else null },
                    beatmap.interested.mapNotNull { osuId -> if (osuId != 0L) userDataSource.find(osuId) else null }
            )
        }

        return FindResponse(
                foundBeatmaps.total,
                foundBeatmaps.count,
                result,
                foundBeatmaps.uuid
        )
    }

    fun updateBeatmap(editorId: Long, beatmapId: Long, updated: UpdatedBeatmap) {
        val beatmap = dataSource.find(beatmapId)
        val oldArtist = beatmap.artist
        val oldTitle = beatmap.title
        val oldMapper = beatmap.mapper
        val oldNote = beatmap.note
        val oldStatus = beatmap.status
        val oldNominators = beatmap.nominators

        beatmap.status = updated.status ?: beatmap.status
        beatmap.artist = updated.artist ?: beatmap.artist
        beatmap.title = updated.title ?: beatmap.title
        beatmap.mapper = updated.mapper ?: beatmap.mapper
        beatmap.note = updated.note ?: beatmap.note
        beatmap.nominators = updated.nominators ?: beatmap.nominators

        if (oldArtist != beatmap.artist || oldTitle != beatmap.title ||
                oldMapper != beatmap.mapper || oldNote != beatmap.note) {
            beatmap.events.add(Events.asBeatmapMetadataModifiedEvent(editorId))
        }

        if (oldNominators != beatmap.nominators) {
            val newNominators = beatmap.nominators.mapNotNull { osuId -> if (osuId != 0L) userDataSource.find(osuId) else null }
            for (oldNominator in oldNominators) {
                if (oldNominator > 0) {
                    val oldNominatorUser = userDataSource.find(oldNominator)
                    if (newNominators.isEmpty()) {
                        beatmap.events.add(Events.asBeatmapNominatorRemovedEvent(editorId, oldNominatorUser))
                    } else {
                        if (!newNominators.any { it.osuId == oldNominator }) {
                            beatmap.events.add(Events.asBeatmapNominatorRemovedEvent(editorId, oldNominatorUser))
                        }
                    }
                }
            }

            newNominators.filter { newNominator -> !oldNominators.any { newNominator.osuId == it }}.forEach {
                beatmap.events.add(Events.asBeatmapNominatorAddedEvent(editorId, it))
            }
        }

        val now = Instant.now().epochSecond
        beatmap.dateUpdated = now

        if (oldStatus != beatmap.status) {
            beatmap.events.add(Events.asBeatmapStatusEvent(editorId, beatmap.status))

            if (beatmap.status == BeatmapStatus.Ranked) {
                beatmap.dateRanked = now
            } else if (oldStatus == BeatmapStatus.Ranked) {
                // When someone accidental sets the map to ranked before
                // We don't want to have an incorrect ranked timestamp but instead reset it back to 0
                beatmap.dateRanked = 0
            }
        }

        dataSource.save(beatmap)
    }

    fun setBeatmapStatus(beatmapId: Long, status: BeatmapStatus) {
        val beatmap = dataSource.find(beatmapId)
        beatmap.status = status
        dataSource.save(beatmap)
    }
}