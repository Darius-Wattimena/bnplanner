package nl.greaper.bnplanner.model.event

import nl.greaper.bnplanner.model.beatmap.BeatmapStatus

data class AiessBeatmapEvent(
    val time: Long, // Epoch seconds
    val beatmapSetId: Long,
    val userId: Long? = null,
    val status: Long = BeatmapStatus.Pending.prio
)