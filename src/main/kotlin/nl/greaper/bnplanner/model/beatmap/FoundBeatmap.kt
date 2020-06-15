package nl.greaper.bnplanner.model.beatmap

import nl.greaper.bnplanner.model.user.User

data class FoundBeatmap (
        val osuId: Long,
        var artist: String,
        var title: String,
        var note: String,
        var mapper: String,
        var status: Long = BeatmapStatus.Pending.prio,
        val nominators: List<User> = emptyList(),
        val interested: List<User> = emptyList(),
        val nominatedByBNOne: Boolean = false,
        val nominatedByBNTwo: Boolean = false
)