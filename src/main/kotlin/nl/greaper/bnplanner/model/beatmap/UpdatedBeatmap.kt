package nl.greaper.bnplanner.model.beatmap

data class UpdatedBeatmap(
        val status: Long?,
        val artist: String?,
        val title: String?,
        val mapper: String?,
        val note: String?,
        val nominators: List<Long?> = emptyList(),
        val nominatedByBNOne: Boolean = false,
        val nominatedByBNTwo: Boolean = false
)