package nl.greaper.bnplanner.service.v2

import mu.KotlinLogging
import nl.greaper.bnplanner.dataSource.BeatmapDataSource
import nl.greaper.bnplanner.model.beatmap.*
import nl.greaper.bnplanner.service.OsuService
import org.springframework.stereotype.Service

@Service
class BeatmapService(
    val dataSource: BeatmapDataSource,
    val osuService: OsuService
) {
    private val log = KotlinLogging.logger {}

    fun findBeatmap(beatmapId: Long): Beatmap? {
        return dataSource.find(beatmapId)
    }

    fun countBeatmaps(
        artist: String?,
        title: String?,
        mapper: String?,
        status: List<Long>,
        nominators: List<Long>,
        page: BeatmapPage,
    ): Int {
        return dataSource.countAll(artist, title, mapper, status, nominators, page)
    }

    fun findBeatmaps(
        artist: String?,
        title: String?,
        mapper: String?,
        status: List<Long>,
        nominators: List<Long>,
        page: BeatmapPage,
        from: Int,
        to: Int
    ): List<FoundBeatmap> {
        val response = dataSource.findAll(artist, title, mapper, status, nominators, from, to, page)

        return response.response.map { it.toFoundBeatmap() }
    }
}