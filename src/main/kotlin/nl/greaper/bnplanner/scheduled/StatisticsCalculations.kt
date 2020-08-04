package nl.greaper.bnplanner.scheduled

import nl.greaper.bnplanner.dataSource.BeatmapDataSource
import nl.greaper.bnplanner.dataSource.StatisticsDataSource
import nl.greaper.bnplanner.dataSource.UserDataSource
import nl.greaper.bnplanner.model.Statistics
import nl.greaper.bnplanner.model.beatmap.BeatmapStatus
import nl.greaper.bnplanner.model.filter.BeatmapFilter
import nl.greaper.bnplanner.model.user.OsuRole
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.ZonedDateTime

@Component
class StatisticsCalculations(
        val dataSource: StatisticsDataSource,
        val beatmapDataSource: BeatmapDataSource,
        val userDataSource: UserDataSource
) {

    // Execute every hour
    @Scheduled(cron = "0 0 * * * *")
    fun calculateDailyStatistics() {
        val filter = BeatmapFilter(
                artist = null,
                title = null,
                mapper = null,
                hideGraved = true,
                hideRanked = true,
                countTotal = false,
                asStatistics = true,
                limit = null,
                page = null
        )

        val totalBeatmapsOnPlanner = beatmapDataSource.countAll()
        val beatmaps = beatmapDataSource.findAll(filter)
        val beatmapsValue = beatmaps.response

        val users = userDataSource.findAll()
        val userPendingIcons = mutableMapOf<Long, Int>()
        val userNominatedIcons = mutableMapOf<Long, Int>()

        for (beatmap in beatmapsValue) {
            var firstNominator = true
            beatmap.nominators.forEach {
                if (it != 0L) {
                    val user = users.find { user -> user.osuId == it }

                    if (user != null && (user.role !== OsuRole.OBS || user.role !== OsuRole.GST)) {
                        userPendingIcons[it] = userPendingIcons[it]?.plus(1) ?: 1

                        if (firstNominator && beatmap.nominatedByBNOne) {
                            userNominatedIcons[it] = userNominatedIcons[it]?.plus(1) ?: 1
                        } else if (!firstNominator && beatmap.nominatedByBNTwo) {
                            userNominatedIcons[it] = userNominatedIcons[it]?.plus(1) ?: 1
                        }
                    }
                }
                firstNominator = false
            }
        }

        val nominatorRoles = listOf(OsuRole.BN, OsuRole.PBN, OsuRole.NAT)

        val statistics = Statistics(
                _id = null,

                totalUser = users.size,
                totalNominators = users.count { user -> nominatorRoles.any { user.role == it } },
                totalFullNominators = users.count { it.role == OsuRole.BN },
                totalProbation = users.count { it.role == OsuRole.PBN },
                totalNATs = users.count { it.role == OsuRole.NAT },
                totalOtherNominators = users.count { it.role == OsuRole.OBS },
                totalGuests = users.count { it.role == OsuRole.GST },

                activePending = beatmapsValue.count { it.status == BeatmapStatus.Pending.prio },
                activeNominated = beatmapsValue.count { it.status == BeatmapStatus.Qualified.prio },
                activeBubbled = beatmapsValue.count { it.status == BeatmapStatus.Bubbled.prio },
                activeDisqualified = beatmapsValue.count { it.status == BeatmapStatus.Disqualified.prio },
                activePopped = beatmapsValue.count { it.status == BeatmapStatus.Popped.prio },

                totalBeatmaps = totalBeatmapsOnPlanner.toInt(),
                totalInProgress = beatmapsValue.count(),
                totalMissingSecondBN = beatmapsValue.count { beatmap -> beatmap.nominators.any { it == 0L } },

                userPendingIcons = userPendingIcons,
                userNominatedIcons = userNominatedIcons,

                timestamp = ZonedDateTime.now().toEpochSecond()
        )

        dataSource.save(statistics)
    }

}