package nl.greaper.bnplanner.util

import nl.greaper.bnplanner.model.beatmap.BeatmapStatus

fun BeatmapStatus.getReadableName(): String {
    return when(this) {
        BeatmapStatus.Pending -> "Pending"
        BeatmapStatus.Bubbled -> "Bubbled"
        BeatmapStatus.Qualified -> "Qualified"
        BeatmapStatus.Popped -> "Popped"
        BeatmapStatus.Disqualified -> "Disqualified"
        BeatmapStatus.Ranked -> "Ranked"
        BeatmapStatus.Graved -> "Graved"
    }
}