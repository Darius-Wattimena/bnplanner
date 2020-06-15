package nl.greaper.bnplanner.model.beatmap

import nl.greaper.bnplanner.model.event.Event
import org.bson.codecs.pojo.annotations.BsonId

data class Beatmap(
        @BsonId
        val osuId: Long,
        var artist: String,
        var title: String,
        var note: String,
        var mapper: String,
        var status: Long = BeatmapStatus.Pending.prio,
        var nominators: MutableList<Long> = mutableListOf(0, 0),
        val interested: MutableList<Long> = mutableListOf(),
        val events: MutableList<Event> = mutableListOf(),
        var dateAdded: Long = 0,
        var dateUpdated: Long = 0,
        var dateRanked: Long = 0,
        var nominatedByBNOne: Boolean = false,
        var nominatedByBNTwo: Boolean = false
)

enum class BeatmapStatus(val prio: Long) {
    Qualified(1),
    Bubbled(2),
    Disqualified(3),
    Popped(4),
    Pending(5),
    Ranked(6),
    Graved(7);

    companion object {
        fun fromPrio(prio: Long): BeatmapStatus
        {
            return when (prio) {
                Qualified.prio -> Qualified
                Bubbled.prio -> Bubbled
                Pending.prio -> Pending
                Disqualified.prio -> Disqualified
                Popped.prio -> Popped
                Ranked.prio -> Ranked
                Graved.prio -> Graved
                else -> Pending
            }
        }
    }
}