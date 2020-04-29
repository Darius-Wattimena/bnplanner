package nl.greaper.bnplanner.model.beatmap

data class UpdatedBeatmap(
        val status: BeatmapStatus?,
        val artist: String?,
        val title: String?,
        val mapper: String?,
        val note: String?,
        val nominators: MutableList<Long>?
)