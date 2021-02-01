package nl.greaper.bnplanner.model.beatmap

data class FoundBeatmap(
        val osuId: Long,
        var artist: String,
        var title: String,
        var note: String,
        var mapper: String,
        var mapperId: Long,
        var status: Long = BeatmapStatus.Unfinished.prio,
        val nominators: List<Long> = emptyList(),
        val interested: List<Long> = emptyList(),
        val nominatedByBNOne: Boolean = false,
        val nominatedByBNTwo: Boolean = false
)