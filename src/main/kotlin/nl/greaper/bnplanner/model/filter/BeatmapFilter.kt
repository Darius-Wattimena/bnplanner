package nl.greaper.bnplanner.model.filter

data class BeatmapFilter(
        val artist: String?,
        val title: String?,
        val mapper: String?,
        val mapperId: Long?,

        val hideGraved: Boolean? = false,
        val hideRanked: Boolean? = false,
        val hideWithTwoNominators: Boolean? = false,

        val limit: BeatmapFilterLimit?,
        val page: Int?,
        val countTotal: Boolean? = false,
        val asStatistics: Boolean = false,
        val statisticsStart: Long? = null,
        val statisticsEnd: Long? = null,

        val status: List<Long> = emptyList(),
        val nominator: List<Long> = emptyList()
)

enum class BeatmapFilterLimit {
    Ten,
    Twenty,
    Fifty;

    fun asNumber(): Int {
        return when(this) {
            Ten -> 10
            Twenty -> 20
            Fifty -> 50
        }
    }
}
