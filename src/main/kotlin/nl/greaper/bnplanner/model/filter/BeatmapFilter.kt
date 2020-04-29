package nl.greaper.bnplanner.model.filter

import nl.greaper.bnplanner.model.beatmap.BeatmapStatus

data class BeatmapFilter(
        val artist: String?,
        val title: String?,
        val mapper: String?,

        val limit: Int?,
        val page: Int?,
        val countTotal: Boolean? = false,
        val hideGraved: Boolean? = false,
        val hideRanked: Boolean? = false,

        val status: List<BeatmapStatus> = emptyList(),
        val nominator: List<Long> = emptyList()
)