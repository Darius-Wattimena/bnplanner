package nl.greaper.bnplanner.dataSource

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import nl.greaper.bnplanner.exception.BeatmapException
import nl.greaper.bnplanner.model.FindResponse
import nl.greaper.bnplanner.model.beatmap.Beatmap
import nl.greaper.bnplanner.model.beatmap.BeatmapStatus
import nl.greaper.bnplanner.model.filter.BeatmapFilter
import nl.greaper.bnplanner.util.quote
import org.litote.kmongo.*
import org.springframework.stereotype.Component

@Component
class BeatmapDataSource(val database: MongoDatabase) {
    fun getCollection(): MongoCollection<Beatmap> = database.getCollection()

    fun save(beatmap: Beatmap) = getCollection().save(beatmap)

    fun exists(beatmapId: Long): Boolean {
        return getCollection().countDocuments(
                Beatmap::osuId eq beatmapId
        ) > 0
    }

    fun deleteById(beatmapId: Long): Boolean {
        return getCollection().deleteOne(Beatmap::osuId eq beatmapId).deletedCount > 0
    }

    fun find(beatmapSetId: Long): Beatmap {
        return getCollection().findOne(
                Beatmap::osuId eq beatmapSetId
        ) ?: throw BeatmapException("Beatmap not registered on the planner")
    }

    fun findAll(filter: BeatmapFilter): FindResponse<Beatmap> {
        val collection = getCollection()

        val query = and(
                and(listOfNotNull(
                        if (filter.artist != null) { Beatmap::artist regex quote(filter.artist).toRegex(RegexOption.IGNORE_CASE) } else null,
                        if (filter.title != null) { Beatmap::title regex quote(filter.title).toRegex(RegexOption.IGNORE_CASE) } else null,
                        if (filter.mapper != null) { Beatmap::mapper regex quote(filter.mapper).toRegex(RegexOption.IGNORE_CASE) } else null,
                        if (filter.nominator.isNotEmpty()) { Beatmap::nominators `in` filter.nominator } else null,
                        if (filter.hideRanked != null && filter.hideRanked) { Beatmap::status ne BeatmapStatus.Ranked.prio } else null,
                        if (filter.hideGraved != null && filter.hideGraved) { Beatmap::status ne BeatmapStatus.Graved.prio } else null,
                        if (filter.hideWithTwoNominators != null && filter.hideWithTwoNominators) { Beatmap::nominators `in` listOf<Long>(0) } else null
                )),
                or(listOfNotNull(
                        if (filter.status.contains(BeatmapStatus.Pending.prio)) Beatmap::status eq BeatmapStatus.Pending.prio else null,
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
            findQuery.limit(filter.limit)
        } else {
            findQuery.limit(10)
        }

        if (filter.page != null && filter.limit != null && filter.page > 0) {
            findQuery.skip((filter.page - 1) * filter.limit)
        }

        val totalCount = if (filter.countTotal != null && filter.countTotal) {
            collection.countDocuments(query).toInt()
        } else {
            0
        }

        findQuery.sort(and(ascending(Beatmap::status), descending(Beatmap::dateUpdated)))

        val result = findQuery.toMutableList()
        return FindResponse(totalCount, result.count(), result)
    }
}