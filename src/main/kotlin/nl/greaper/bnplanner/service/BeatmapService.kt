package nl.greaper.bnplanner.service

import nl.greaper.bnplanner.DiscordWebhookClient
import nl.greaper.bnplanner.dataSource.BeatmapDataSource
import nl.greaper.bnplanner.dataSource.UserDataSource
import nl.greaper.bnplanner.exception.BeatmapException
import nl.greaper.bnplanner.model.FindResponse
import nl.greaper.bnplanner.model.beatmap.*
import nl.greaper.bnplanner.model.discord.EmbedColor
import nl.greaper.bnplanner.model.discord.EmbedFooter
import nl.greaper.bnplanner.model.discord.EmbedThumbnail
import nl.greaper.bnplanner.model.event.Events
import nl.greaper.bnplanner.model.filter.BeatmapFilter
import nl.greaper.bnplanner.model.user.User
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class BeatmapService(
        val dataSource: BeatmapDataSource,
        val userDataSource: UserDataSource,
        val osuService: OsuService,
        val discordWebhookClient: DiscordWebhookClient
) {
    fun addBeatmap(editor: User, beatmapId: Long, token: String): Boolean {
        if (dataSource.exists(beatmapId)) {
            throw BeatmapException("Beatmapset already registered on the planner")
        }
        val now = Instant.now().epochSecond
        val beatmapSet = osuService.findBeatmapSetInfo(token, beatmapId)
        return if (beatmapSet != null) {
            val newBeatmap = Beatmap(beatmapId, beatmapSet.artist, beatmapSet.title, "", beatmapSet.creator, dateAdded = now, dateUpdated = now)
            dataSource.save(newBeatmap)
            newBeatmap.plannerEvents.add(Events.asBeatmapCreatedEvent(editor.osuId))
            dataSource.save(newBeatmap)
            discordWebhookClient.send(
                    """🌟 **Created**
                        **[${newBeatmap.artist} - ${newBeatmap.title}](https://osu.ppy.sh/beatmapsets/${newBeatmap.osuId})**
                        Mapped by [${newBeatmap.mapper}](https://osu.ppy.sh/users/${newBeatmap.mapper})
                    """.prependIndent(),
                    EmbedColor.GREEN,
                    EmbedThumbnail("https://b.ppy.sh/thumb/${newBeatmap.osuId}l.jpg"),
                    EmbedFooter(editor.osuName, editor.profilePictureUri)
            )
            true
        } else {
            false
        }
    }

    fun findBeatmap(beatmapId: Long): Beatmap {
        return dataSource.find(beatmapId)
    }

    fun deleteBeatmap(beatmapId: Long, editor: User): Boolean {
        val beatmap = dataSource.find(beatmapId)
        val result = dataSource.deleteById(beatmapId)

        discordWebhookClient.send(
                """🐒 **Deleted**
                    **[${beatmap.artist} - ${beatmap.title}](https://osu.ppy.sh/beatmapsets/${beatmap.osuId})**
                    Mapped by [${beatmap.mapper}](https://osu.ppy.sh/users/${beatmap.mapper})
                """.prependIndent(),
                EmbedColor.RED,
                EmbedThumbnail("https://b.ppy.sh/thumb/${beatmap.osuId}l.jpg"),
                EmbedFooter(editor.osuName, editor.profilePictureUri),
                confidential = true
        )

        return result
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
                beatmap.plannerEvents,
                beatmap.osuEvents,
                beatmap.nominatedByBNOne,
                beatmap.nominatedByBNTwo
        )
    }

    fun findBeatmaps(filter: BeatmapFilter): FindResponse<FoundBeatmap> {
        val foundBeatmaps = dataSource.findAll(filter)
        val result = foundBeatmaps.response.map { beatmap ->
            FoundBeatmap(
                    beatmap.osuId,
                    beatmap.artist,
                    beatmap.title,
                    beatmap.note,
                    beatmap.mapper,
                    beatmap.status,
                    beatmap.nominators,
                    beatmap.interested,
                    beatmap.nominatedByBNOne,
                    beatmap.nominatedByBNTwo
            )
        }

        return FindResponse(
                foundBeatmaps.total,
                foundBeatmaps.count,
                result,
                foundBeatmaps.uuid
        )
    }

    //TODO write unit test
    fun updateBeatmap(editor: User, beatmapId: Long, updated: UpdatedBeatmap) {
        val databaseBeatmap = dataSource.find(beatmapId)
        val oldArtist = databaseBeatmap.artist
        val oldTitle = databaseBeatmap.title
        val oldMapper = databaseBeatmap.mapper
        val oldNote = databaseBeatmap.note
        val oldStatus = databaseBeatmap.status
        val oldNominators = databaseBeatmap.nominators

        val updatedBeatmap = databaseBeatmap.copy(
                status = updated.status ?: databaseBeatmap.status,
                artist = updated.artist ?: databaseBeatmap.artist ,
                title = updated.title ?: databaseBeatmap.title,
                mapper = updated.mapper ?: databaseBeatmap.mapper,
                note = updated.note ?: databaseBeatmap.note,
                nominators = updated.nominators.map { it ?: 0 },
                nominatedByBNOne = updated.nominatedByBNOne,
                nominatedByBNTwo = updated.nominatedByBNTwo
        )

        if (oldArtist != updatedBeatmap.artist || oldTitle != updatedBeatmap.title ||
                oldMapper != updatedBeatmap.mapper) {
            updatedBeatmap.plannerEvents.add(Events.asBeatmapMetadataModifiedEvent(editor.osuId))
        }

        if (oldNote != updatedBeatmap.note) {
            updatedBeatmap.plannerEvents.add(Events.asBeatmapNoteModifiedEvent(editor.osuId))
        }

        val addedNominators = updatedBeatmap.nominators.filter { !oldNominators.contains(it) }
                .mapNotNull { osuId -> if (osuId != 0L) userDataSource.find(osuId) else null }
        val removedNominators = oldNominators.filter { !updatedBeatmap.nominators.contains(it) }
                .mapNotNull { osuId -> if (osuId != 0L) userDataSource.find(osuId) else null }

        logNominatorChanges(addedNominators, removedNominators, updatedBeatmap, editor)

        val now = Instant.now().epochSecond
        var nominatedByBNOne = updatedBeatmap.nominatedByBNOne
        var nominatedByBNTwo = updatedBeatmap.nominatedByBNTwo

        // When a nominator is remove we set their respective flag to false
        if (!updatedBeatmap.nominators.isNullOrEmpty()) {
            val nominatorOne = updatedBeatmap.nominators[0]
            val nominatorTwo = updatedBeatmap.nominators[1]

            if (nominatorOne == 0L) {
                nominatedByBNOne = false
            }

            if (nominatorTwo == 0L) {
                nominatedByBNTwo = false
            }
        }

        val newStatus = determineNewStatus(nominatedByBNOne, nominatedByBNTwo, updatedBeatmap)

        // Now check if the status got updated so we can update the rank date and add an event
        val dateRanked = if (oldStatus != newStatus) {
            when (newStatus) {
                BeatmapStatus.Pending.prio, BeatmapStatus.Graved.prio -> {
                    updatedBeatmap.plannerEvents.add(Events.asBeatmapStatusEvent(editor.osuId, newStatus))
                }
                else -> {
                    updatedBeatmap.osuEvents.add(Events.asBeatmapStatusEvent(editor.osuId, newStatus))
                }
            }

            when {
                newStatus == BeatmapStatus.Ranked.prio -> {
                    now
                }
                oldStatus != BeatmapStatus.Ranked.prio -> {
                    // When someone accidental sets the map to ranked before
                    // We don't want to have an incorrect ranked timestamp but instead reset it back to 0
                    0
                }
                else -> {
                    updatedBeatmap.dateRanked
                }
            }
        } else {
            updatedBeatmap.dateRanked
        }

        dataSource.save(updatedBeatmap.copy(
                nominatedByBNOne = nominatedByBNOne,
                nominatedByBNTwo = nominatedByBNTwo,
                status = newStatus,
                dateUpdated = now,
                dateRanked = dateRanked
        ))
    }

    /**
     * Updated the beatmap status for users that are lazy
     * - To Pending when no nominator nominated the set and is not popped or disqualified
     * - To Bubbled when only 1 nominator nominated the set
     * - To Qualified when 2 nominators nominated the set
     */
    private fun determineNewStatus(nominatedByBNOne: Boolean, nominatedByBNTwo: Boolean, updatedBeatmap: Beatmap): Long {
        return when(nominatedByBNOne to nominatedByBNTwo) {
            true to true -> {
                if (updatedBeatmap.status != BeatmapStatus.Ranked.prio) {
                    BeatmapStatus.Qualified.prio
                } else {
                    updatedBeatmap.status
                }
            }
            true to false, false to true -> {
                BeatmapStatus.Bubbled.prio
            }
            false to false -> {
                if (updatedBeatmap.status != BeatmapStatus.Popped.prio && updatedBeatmap.status != BeatmapStatus.Disqualified.prio) {
                    BeatmapStatus.Pending.prio
                } else{
                    updatedBeatmap.status
                }
            }
            else -> updatedBeatmap.status
        }
    }

    /**
     * Log nominator changes to the planner events and also push a message to discord
     */
    private fun logNominatorChanges(addedNominators: List<User>, removedNominators: List<User>, updatedBeatmap: Beatmap, editor: User) {
        var nominatorChangesText = ""
        var firstItem = true

        addedNominators.forEach {
            updatedBeatmap.plannerEvents.add(Events.asBeatmapNominatorAddedEvent(editor.osuId, it))
            if (firstItem) {
                firstItem = false
            } else {
                nominatorChangesText += "\n"
            }

            //✅ 💡
            nominatorChangesText += "✅ **Added [${it.osuName}](https://osu.ppy.sh/users/${it.osuId})**"
        }

        removedNominators.forEach {
            updatedBeatmap.plannerEvents.add(Events.asBeatmapNominatorRemovedEvent(editor.osuId, it))
            if (firstItem) {
                firstItem = false
            } else {
                nominatorChangesText += "\n"
            }

            // ❎ ❌ 💥
            nominatorChangesText += "❌ **Removed [${it.osuName}](https://osu.ppy.sh/users/${it.osuId})**"
        }

        if (removedNominators.isNotEmpty() || addedNominators.isNotEmpty()) {
            discordWebhookClient.send(
                    """$nominatorChangesText
                            **[${updatedBeatmap.artist} - ${updatedBeatmap.title}](https://osu.ppy.sh/beatmapsets/${updatedBeatmap.osuId})**
                            Mapped by [${updatedBeatmap.mapper}](https://osu.ppy.sh/users/${updatedBeatmap.mapper})
                        """.prependIndent(),
                    EmbedColor.BLUE,
                    EmbedThumbnail("https://b.ppy.sh/thumb/${updatedBeatmap.osuId}l.jpg"),
                    EmbedFooter(editor.osuName, editor.profilePictureUri)
            )
        }
    }

    fun setBeatmapStatus(editorId: Long, beatmapId: Long, statusUpdate: UpdatedBeatmapStatus) {
        val databaseBeatmap = dataSource.find(beatmapId)
        val newStatus = statusUpdate.status

        // Status didn't change so we can ignore
        if (databaseBeatmap.status == newStatus) {
            return
        }

        val now = Instant.now().epochSecond
        var nominatedByBNOne = databaseBeatmap.nominatedByBNOne
        var nominatedByBNTwo = databaseBeatmap.nominatedByBNTwo
        var dateRanked = 0L

        when(newStatus) {
            BeatmapStatus.Popped.prio, BeatmapStatus.Disqualified.prio -> {
                val reason = statusUpdate.reason ?: ""

                if (newStatus == BeatmapStatus.Popped.prio) {
                    databaseBeatmap.osuEvents.add(Events.asBeatmapPoppedEvent(editorId, reason))
                } else {
                    databaseBeatmap.osuEvents.add(Events.asBeatmapDisqualifiedEvent(editorId, reason))
                }

                nominatedByBNOne = false
                nominatedByBNTwo = false
            }
            BeatmapStatus.Ranked.prio -> {
                databaseBeatmap.osuEvents.add(Events.asBeatmapStatusEvent(editorId, newStatus))
                dateRanked = now
                nominatedByBNOne = true
                nominatedByBNTwo = true
            }
            else -> {
                databaseBeatmap.plannerEvents.add(Events.asBeatmapStatusEvent(editorId, newStatus))
            }
        }

        dataSource.save(databaseBeatmap.copy(
                nominatedByBNOne = nominatedByBNOne,
                nominatedByBNTwo = nominatedByBNTwo,
                dateUpdated = now,
                dateRanked = dateRanked,
                status = newStatus
        ))
    }
}