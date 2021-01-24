package nl.greaper.bnplanner.model.osu

data class BeatmapSet(
        val id: Long,
        val title: String,
        val artist: String,
        val creator: String,
        val user_id: Long
)