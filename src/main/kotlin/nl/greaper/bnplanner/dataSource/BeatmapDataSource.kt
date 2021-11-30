package nl.greaper.bnplanner.dataSource

import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.IndexOptions
import mu.KotlinLogging
import nl.greaper.bnplanner.model.LegacyFindResponse
import nl.greaper.bnplanner.model.beatmap.Beatmap
import nl.greaper.bnplanner.model.beatmap.BeatmapPage
import nl.greaper.bnplanner.model.beatmap.BeatmapStatus
import nl.greaper.bnplanner.model.filter.BeatmapFilter
import nl.greaper.bnplanner.util.logIfNull
import nl.greaper.bnplanner.util.quote
import org.bson.conversions.Bson
import org.litote.kmongo.*
import org.springframework.stereotype.Component

@Component
class BeatmapDataSource(database: MongoDatabase) {
    private val collection = database.getCollection("beatmap", Beatmap::class.java)
    private val log = KotlinLogging.logger {}

    init {
        collection.ensureIndex(Beatmap::artist)
        collection.ensureIndex(Beatmap::title)
        collection.ensureIndex(Beatmap::mapper)
        collection.ensureIndex(Beatmap::nominators)
        collection.ensureIndex(Beatmap::status)
        collection.ensureIndex(Beatmap::dateUpdated)
    }

    fun save(beatmap: Beatmap) = collection.save(beatmap)

    fun exists(beatmapId: Long): Boolean {
        return collection.countDocuments(
                Beatmap::osuId eq beatmapId
        ) > 0
    }

    fun deleteById(beatmapId: Long): Boolean {
        return collection.deleteOne(Beatmap::osuId eq beatmapId).deletedCount > 0
    }

    fun find(beatmapSetId: Long): Beatmap? {
        val beatmap = collection.findOneById(beatmapSetId).logIfNull(log) {
            "Could not find the beatmap in database with BeatmapSetID = $beatmapSetId"
        }

        return beatmap
    }

    fun setupFilter(
        artist: String?,
        title: String?,
        mapper: String?,
        status: List<Long>,
        nominators: List<Long>,
        page: BeatmapPage
    ): Bson {
        val filters = mutableListOf<Bson>()

        artist?.let { filters += Beatmap::artist regex quote(it).toRegex(RegexOption.IGNORE_CASE) }
        title?.let { filters += Beatmap::title regex quote(it).toRegex(RegexOption.IGNORE_CASE) }
        mapper?.let { filters += Beatmap::mapper regex quote(it).toRegex(RegexOption.IGNORE_CASE) }

        if (nominators.isNotEmpty()) {
            filters += Beatmap::nominators `in` nominators
        }

        if (status.isNotEmpty()) {
            filters += or(status.map { Beatmap::status eq it })
        } else {
            when (page) {
                BeatmapPage.PENDING -> filters += listOf(
                    Beatmap::status ne BeatmapStatus.Ranked.prio,
                    Beatmap::status ne BeatmapStatus.Graved.prio
                )
                BeatmapPage.RANKED -> filters += Beatmap::status eq BeatmapStatus.Ranked.prio
                BeatmapPage.GRAVEYARD -> filters += Beatmap::status eq BeatmapStatus.Graved.prio
            }
        }

        return and(filters)
    }

    fun countAll(
        artist: String?,
        title: String?,
        mapper: String?,
        status: List<Long>,
        nominators: List<Long>,
        page: BeatmapPage
    ): Int {
        val filter = setupFilter(artist, title, mapper, status, nominators, page)
        return collection.countDocuments(filter).toInt()
    }

    fun findAllInitial(
        artist: String?,
        title: String?,
        mapper: String?,
        status: List<Long>,
        nominators: List<Long>,
        limit: Int,
        page: BeatmapPage
    ): LegacyFindResponse<Beatmap> {
        val filter = setupFilter(artist, title, mapper, status, nominators, page)

        val findQuery = collection.find(filter)
        findQuery.limit(limit)
        findQuery.sort(and(ascending(Beatmap::status), descending(Beatmap::dateUpdated)))

        val totalData = collection.countDocuments(filter).toInt()
        val result = findQuery.toMutableList()

        return LegacyFindResponse(totalData, result.count(), result)
    }

    fun findAll(
        artist: String?,
        title: String?,
        mapper: String?,
        status: List<Long>,
        nominators: List<Long>,
        from: Int,
        to: Int,
        page: BeatmapPage
    ): LegacyFindResponse<Beatmap> {
        val filter = setupFilter(artist, title, mapper, status, nominators, page)

        val findQuery = collection.find(filter)
        findQuery.limit(to - from)
        findQuery.skip(from)
        findQuery.sort(and(ascending(Beatmap::status), descending(Beatmap::dateUpdated)))
        val result = findQuery.toMutableList()

        return LegacyFindResponse(0, result.count(), result)
    }

    fun findAll(filter: BeatmapFilter): LegacyFindResponse<Beatmap> {
        val query = and(
                and(listOfNotNull(
                        if (filter.artist != null) { Beatmap::artist regex quote(filter.artist).toRegex(RegexOption.IGNORE_CASE) } else null,
                        if (filter.title != null) { Beatmap::title regex quote(filter.title).toRegex(RegexOption.IGNORE_CASE) } else null,
                        if (filter.mapper != null) { Beatmap::mapper regex quote(filter.mapper).toRegex(RegexOption.IGNORE_CASE) } else null,
                        if (filter.mapperId != null) { Beatmap::mapperId eq filter.mapperId } else null,
                        if (filter.nominator.any { it != 0L }) { Beatmap::nominators `in` filter.nominator.filter { it != 0L } } else null,
                        if (filter.hideRanked != null && !filter.hideRanked) null else Beatmap::status ne BeatmapStatus.Ranked.prio,
                        if (filter.hideGraved != null && !filter.hideGraved) null else Beatmap::status ne BeatmapStatus.Graved.prio,
                        if (filter.hideWithTwoNominators != null && filter.hideWithTwoNominators) { Beatmap::nominators `in` listOf<Long>(0) } else null,
                        if (filter.statisticsStart != null) { or(
                                Beatmap::dateUpdated gte filter.statisticsStart
                        ) } else null,
                        if (filter.statisticsEnd != null) { or(
                                Beatmap::dateUpdated lte filter.statisticsEnd
                        ) } else null
                )),
                or(listOfNotNull(
                        if (filter.status.contains(BeatmapStatus.Pending.prio)) Beatmap::status eq BeatmapStatus.Pending.prio else null,
                        if (filter.status.contains(BeatmapStatus.Unfinished.prio)) Beatmap::status eq BeatmapStatus.Unfinished.prio else null,
                        if (filter.status.contains(BeatmapStatus.Bubbled.prio)) Beatmap::status eq BeatmapStatus.Bubbled.prio else null,
                        if (filter.status.contains(BeatmapStatus.Qualified.prio)) Beatmap::status eq BeatmapStatus.Qualified.prio else null,
                        if (filter.status.contains(BeatmapStatus.Ranked.prio)) Beatmap::status eq BeatmapStatus.Ranked.prio else null,
                        if (filter.status.contains(BeatmapStatus.Popped.prio)) Beatmap::status eq BeatmapStatus.Popped.prio else null,
                        if (filter.status.contains(BeatmapStatus.Disqualified.prio)) Beatmap::status eq BeatmapStatus.Disqualified.prio else null,
                        if (filter.status.contains(BeatmapStatus.Graved.prio)) Beatmap::status eq BeatmapStatus.Graved.prio else null
                ))
        )

        val findQuery = collection.find(query)

        if (filter.limit != null) {
            findQuery.limit(filter.limit.asNumber())

            if (filter.page != null && filter.page > 0) {
                findQuery.skip((filter.page - 1) * filter.limit.asNumber())
            }
        } else {
            if (!filter.asStatistics) {
                findQuery.limit(10)
            }
        }

        val totalCount = if (filter.countTotal != null && filter.countTotal) {
            collection.countDocuments(query).toInt()
        } else {
            0
        }

        findQuery.sort(and(ascending(Beatmap::status), descending(Beatmap::dateUpdated)))

        val result = findQuery.toMutableList()
        return LegacyFindResponse(totalCount, result.count(), result)
    }

    fun countAll(): Long {
        return collection.countDocuments()
    }
}