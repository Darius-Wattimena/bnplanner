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
        var status: BeatmapStatus = BeatmapStatus.AwaitingResponse,
        var nominators: MutableList<Long> = mutableListOf(0, 0),
        val interested: MutableList<Long> = mutableListOf(),
        val events: MutableList<Event> = mutableListOf(),
        var dateAdded: Long = 0,
        var dateUpdated: Long = 0,
        var dateRanked: Long = 0
)

enum class BeatmapStatus {
    Qualified,
    Bubbled,
    Pending,
    AwaitingResponse,
    WorkInProgress,
    Popped,
    Disqualified,
    Ranked,
    Graved;
}