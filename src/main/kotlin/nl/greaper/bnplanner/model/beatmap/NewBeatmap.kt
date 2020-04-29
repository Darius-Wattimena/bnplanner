package nl.greaper.bnplanner.model.beatmap

data class NewBeatmap(
        val beatmapId: String,
        val artist: String,
        val title: String,
        val mapper: String
)