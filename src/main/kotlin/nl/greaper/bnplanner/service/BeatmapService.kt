package nl.greaper.bnplanner.service

import mu.KotlinLogging
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
import nl.greaper.bnplanner.util.getReadableName
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class BeatmapService(
        val dataSource: BeatmapDataSource,
        val userDataSource: UserDataSource,
        val osuService: OsuService,
        val discordWebhookClient: DiscordWebhookClient
) {
    private val UPDATED_STATUS_ICON = "\uD83D\uDCAD" // 💭
    private val RANKED_STATUS_ICON = "\uD83D\uDC96" // 💖
    private val NOMINATED_STATUS_ICON = "❤"
    private val BUBBLED_STATUS_ICON = "\uD83D\uDCAD" // 💭
    private val DISQUALIFIED_STATUS_ICON = "\uD83D\uDC94" // 💔
    private val POPPED_STATUS_ICON = "\uD83D\uDDEF" // 🗯️
    private val GRAVED_STATUS_ICON = "\uD83D\uDDD1" // 🗑️
    private val UNFINISHED_STATUS_ICON = "\uD83D\uDD28" // 🔨

    private val CREATED_BEATMAP_ICON = "\uD83C\uDF1F" // 🌟
    private val DELETED_BEATMAP_ICON = "\uD83D\uDC12" // 🐒
    private val ADDED_NOMINATOR_ICON = "✅"
    private val REMOVED_NOMINATOR_ICON = "❌"

    private val log = KotlinLogging.logger {}

    fun addBeatmap(editor: User, beatmapId: Long, token: String): Boolean {
        if (dataSource.exists(beatmapId)) {
            log.info { "Beatmapset with id $beatmapId is already registered to the planner" }
        }
        val now = Instant.now().epochSecond
        val beatmapSet = osuService.findBeatmapSetInfo(token, beatmapId)
        return if (beatmapSet != null) {
            val newBeatmap = Beatmap(beatmapId, beatmapSet.artist, beatmapSet.title, "", beatmapSet.creator, beatmapSet.user_id, dateAdded = now, dateUpdated = now)
            dataSource.save(newBeatmap)
            newBeatmap.plannerEvents.add(Events.asBeatmapCreatedEvent(editor.osuId))
            dataSource.save(newBeatmap)
            discordWebhookClient.send(
                    """$CREATED_BEATMAP_ICON **Created**
                        **[${newBeatmap.artist} - ${newBeatmap.title}](https://osu.ppy.sh/beatmapsets/${newBeatmap.osuId})**
                        Mapped by [${newBeatmap.mapper}](https://osu.ppy.sh/users/${newBeatmap.mapper.replace(" ", "%20")})
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

    fun findBeatmap(beatmapId: Long): Beatmap? {
        return dataSource.find(beatmapId)
    }

    fun deleteBeatmap(beatmapId: Long, editor: User): Boolean {
        val beatmap = dataSource.find(beatmapId) ?: return false
        val result = dataSource.deleteById(beatmapId)

        discordWebhookClient.send(
                """$DELETED_BEATMAP_ICON **Deleted**
                    **[${beatmap.artist} - ${beatmap.title}](https://osu.ppy.sh/beatmapsets/${beatmap.osuId})**
                    Mapped by [${beatmap.mapper}](https://osu.ppy.sh/users/${beatmap.mapper.replace(" ", "%20")})
                """.prependIndent(),
                EmbedColor.RED,
                EmbedThumbnail("https://b.ppy.sh/thumb/${beatmap.osuId}l.jpg"),
                EmbedFooter(editor.osuName, editor.profilePictureUri),
                confidential = true
        )

        return result
    }

    fun refreshMetadata(editor: User, beatmapId: Long, token: String): Boolean {
        val beatmap = dataSource.find(beatmapId) ?: return false
        val beatmapSet = osuService.findBeatmapSetInfo(token, beatmapId)

        if (beatmapSet != null) {
            val updatedBeatmap = beatmap.copy(
                    mapper = beatmapSet.creator,
                    artist = beatmapSet.artist,
                    title = beatmapSet.title
            )

            dataSource.save(updatedBeatmap)

            return true
        } else {
            log.info { "Could not find the beatmap on the osu! api with BeatmapSetID = $beatmapId" }
            return false
        }
    }

    fun findDetailedBeatmap(beatmapId: Long): DetailedBeatmap? {
        val beatmap = dataSource.find(beatmapId) ?: return null

        return DetailedBeatmap(
                beatmap.osuId,
                beatmap.artist,
                beatmap.title,
                beatmap.note,
                beatmap.mapper,
                beatmap.mapperId,
                beatmap.status,
                beatmap.nominators,
                beatmap.interested,
                beatmap.plannerEvents,
                beatmap.osuEvents,
                beatmap.nominatedByBNOne,
                beatmap.nominatedByBNTwo,
                beatmap.dateUpdated,
                beatmap.unfinished
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
                    beatmap.mapperId,
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
        val databaseBeatmap = dataSource.find(beatmapId) ?: return
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
                nominatedByBNTwo = updated.nominatedByBNTwo,
                unfinished = updated.unfinished
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
        logNewStatus(oldStatus, newStatus, updatedBeatmap, editor)

        val dateRanked = determineDateRanked(oldStatus, newStatus, updatedBeatmap, editor, now)

        dataSource.save(updatedBeatmap.copy(
                nominatedByBNOne = nominatedByBNOne,
                nominatedByBNTwo = nominatedByBNTwo,
                status = newStatus,
                dateUpdated = now,
                dateRanked = dateRanked
        ))
    }

    private fun logNewStatus(oldStatus: Long, newStatus: Long, updatedBeatmap: Beatmap, editor: User) {
        if (oldStatus == newStatus) return
        else {
            val messageIcon = getMessageIcon(newStatus)

            discordWebhookClient.send(
                    """**$messageIcon Updated status to ${BeatmapStatus.fromPrio(newStatus).getReadableName()}**
                        **[${updatedBeatmap.artist} - ${updatedBeatmap.title}](https://osu.ppy.sh/beatmapsets/${updatedBeatmap.osuId})**
                        Mapped by [${updatedBeatmap.mapper}](https://osu.ppy.sh/users/${updatedBeatmap.mapper.replace(" ", "%20")})
                    """.prependIndent(),
                    EmbedColor.BLUE,
                    EmbedThumbnail("https://b.ppy.sh/thumb/${updatedBeatmap.osuId}l.jpg"),
                    EmbedFooter(editor.osuName, editor.profilePictureUri),
                    confidential = true
            )
        }
    }

    /**
     * Check if the status got updated so we can update the rank date and add an event
     */
    private fun determineDateRanked(oldStatus: Long, newStatus: Long, updatedBeatmap: Beatmap, editor: User, now: Long): Long {
        return if (oldStatus != newStatus) {
            when (newStatus) {
                BeatmapStatus.Pending.prio, BeatmapStatus.Unfinished.prio, BeatmapStatus.Graved.prio -> {
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
                    if (updatedBeatmap.unfinished) BeatmapStatus.Unfinished.prio else BeatmapStatus.Pending.prio
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

            nominatorChangesText += "$ADDED_NOMINATOR_ICON **Added [${it.osuName}](https://osu.ppy.sh/users/${it.osuId})**"
        }

        removedNominators.forEach {
            updatedBeatmap.plannerEvents.add(Events.asBeatmapNominatorRemovedEvent(editor.osuId, it))
            if (firstItem) {
                firstItem = false
            } else {
                nominatorChangesText += "\n"
            }

            nominatorChangesText += "$REMOVED_NOMINATOR_ICON **Removed [${it.osuName}](https://osu.ppy.sh/users/${it.osuId})**"
        }

        if (removedNominators.isNotEmpty() || addedNominators.isNotEmpty()) {
            discordWebhookClient.send(
                    """$nominatorChangesText
                        **[${updatedBeatmap.artist} - ${updatedBeatmap.title}](https://osu.ppy.sh/beatmapsets/${updatedBeatmap.osuId})**
                        Mapped by [${updatedBeatmap.mapper}](https://osu.ppy.sh/users/${updatedBeatmap.mapper.replace(" ", "%20")})
                    """.prependIndent(),
                    EmbedColor.BLUE,
                    EmbedThumbnail("https://b.ppy.sh/thumb/${updatedBeatmap.osuId}l.jpg"),
                    EmbedFooter(editor.osuName, editor.profilePictureUri)
            )
        }
    }

    fun setBeatmapStatus(editor: User, beatmapId: Long, statusUpdate: UpdatedBeatmapStatus) {
        val databaseBeatmap = dataSource.find(beatmapId) ?: return
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
                    databaseBeatmap.osuEvents.add(Events.asBeatmapPoppedEvent(editor.osuId, reason))
                } else {
                    databaseBeatmap.osuEvents.add(Events.asBeatmapDisqualifiedEvent(editor.osuId, reason))
                }

                nominatedByBNOne = false
                nominatedByBNTwo = false
            }
            BeatmapStatus.Ranked.prio -> {
                databaseBeatmap.osuEvents.add(Events.asBeatmapStatusEvent(editor.osuId, newStatus))
                dateRanked = now
                nominatedByBNOne = true
                nominatedByBNTwo = true
            }
            else -> {
                databaseBeatmap.plannerEvents.add(Events.asBeatmapStatusEvent(editor.osuId, newStatus))
            }
        }

        val messageIcon = getMessageIcon(newStatus)

        discordWebhookClient.send(
                """**$messageIcon Updated status to ${BeatmapStatus.fromPrio(newStatus).getReadableName()}**
                            **[${databaseBeatmap.artist} - ${databaseBeatmap.title}](https://osu.ppy.sh/beatmapsets/${databaseBeatmap.osuId})**
                            Mapped by [${databaseBeatmap.mapper}](https://osu.ppy.sh/users/${databaseBeatmap.mapper.replace(" ", "%20")})
                        """.prependIndent(),
                EmbedColor.ORANGE,
                EmbedThumbnail("https://b.ppy.sh/thumb/${databaseBeatmap.osuId}l.jpg"),
                EmbedFooter(editor.osuName, editor.profilePictureUri),
                confidential = true
        )

        dataSource.save(databaseBeatmap.copy(
                nominatedByBNOne = nominatedByBNOne,
                nominatedByBNTwo = nominatedByBNTwo,
                dateUpdated = now,
                dateRanked = dateRanked,
                status = newStatus
        ))
    }

    private fun getMessageIcon(status: Long): String {
        return when (BeatmapStatus.fromPrio(status)) {
            BeatmapStatus.Qualified -> NOMINATED_STATUS_ICON
            BeatmapStatus.Bubbled -> BUBBLED_STATUS_ICON
            BeatmapStatus.Disqualified -> DISQUALIFIED_STATUS_ICON
            BeatmapStatus.Popped -> POPPED_STATUS_ICON
            BeatmapStatus.Pending -> UPDATED_STATUS_ICON
            BeatmapStatus.Ranked -> RANKED_STATUS_ICON
            BeatmapStatus.Graved -> GRAVED_STATUS_ICON
            BeatmapStatus.Unfinished -> UNFINISHED_STATUS_ICON
        }
    }
}