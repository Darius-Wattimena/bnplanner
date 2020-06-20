package nl.greaper.bnplanner.model.filter

data class BeatmapFilter(
        val artist: String?,
        val title: String?,
        val mapper: String?,

        val limit: Int?,
        val page: Int?,
        val countTotal: Boolean? = false,
        val hideGraved: Boolean? = false,
        val hideRanked: Boolean? = false,
        val hideWithTwoNominators: Boolean? = false,

        val status: List<Long> = emptyList(),
        val nominator: List<Long> = emptyList()
)