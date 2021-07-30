package nl.greaper.bnplanner.model.beatmap

import nl.greaper.bnplanner.model.event.AiessBeatmapEvent
import nl.greaper.bnplanner.model.event.Event
import org.bson.codecs.pojo.annotations.BsonId

data class Beatmap(
        @BsonId
        val osuId: Long,
        val artist: String,
        val title: String,
        val note: String,
        val mapper: String,
        var mapperId: Long = 0,
        val status: Long = BeatmapStatus.Pending.prio,
        val nominators: List<Long> = listOf(0, 0),
        val interested: MutableList<Long> = mutableListOf(),
        val plannerEvents: MutableList<Event> = mutableListOf(),
        val osuEvents: MutableList<Event> = mutableListOf(),
        val aiessEvents: MutableList<AiessBeatmapEvent> = mutableListOf(),
        val dateAdded: Long = 0,
        val dateUpdated: Long = 0,
        val dateRanked: Long = 0,
        val nominatedByBNOne: Boolean = false,
        val nominatedByBNTwo: Boolean = false,
        val unfinished: Boolean = false
)

enum class BeatmapStatus(val prio: Long) {
    Qualified(1),
    Bubbled(2),
    Disqualified(3),
    Popped(4),
    Pending(5),
    Ranked(6),
    Graved(7),
    Unfinished(8);

    companion object {
        fun fromPrio(prio: Long): BeatmapStatus?
        {
            return when (prio) {
                Qualified.prio -> Qualified
                Bubbled.prio -> Bubbled
                Pending.prio -> Pending
                Disqualified.prio -> Disqualified
                Popped.prio -> Popped
                Ranked.prio -> Ranked
                Graved.prio -> Graved
                Unfinished.prio -> Unfinished
                else -> null
            }
        }
    }
}