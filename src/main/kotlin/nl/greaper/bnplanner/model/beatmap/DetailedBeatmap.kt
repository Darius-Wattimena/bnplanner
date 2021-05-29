package nl.greaper.bnplanner.model.beatmap

data class DetailedBeatmap(
        val osuId: Long,
        val artist: String,
        val title: String,
        val note: String,
        val mapper: String,
        val mapperId: Long,
        val status: Long,
        val nominators: List<Long>,
        val interested: MutableList<Long>,
        val nominatedByBNOne: Boolean = false,
        val nominatedByBNTwo: Boolean = false,
        val dateUpdated: Long,
        val unfinished: Boolean = false
)