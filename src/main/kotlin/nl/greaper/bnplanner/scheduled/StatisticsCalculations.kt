package nl.greaper.bnplanner.scheduled

import nl.greaper.bnplanner.dataSource.BeatmapDataSource
import nl.greaper.bnplanner.dataSource.StatisticsDataSource
import nl.greaper.bnplanner.dataSource.UserDataSource
import nl.greaper.bnplanner.dataSource.UserStatisticsDataSource
import nl.greaper.bnplanner.model.Statistics
import nl.greaper.bnplanner.model.UserStatistics
import nl.greaper.bnplanner.model.UserStatistics.*
import nl.greaper.bnplanner.model.beatmap.BeatmapStatus
import nl.greaper.bnplanner.model.filter.BeatmapFilter
import nl.greaper.bnplanner.model.user.OsuRole
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters.firstDayOfYear
import java.time.temporal.TemporalAdjusters.lastDayOfYear

@Component
class StatisticsCalculations(
        val statisticsDataSource: StatisticsDataSource,
        val userStatisticsDataSource: UserStatisticsDataSource,
        val beatmapDataSource: BeatmapDataSource,
        val userDataSource: UserDataSource
) {
    fun generateUserStatistics(start: Instant, end: Instant, statisticsType: StatisticsType, now: Instant): List<UserStatistics> {
        val filter = BeatmapFilter(
                artist = null,
                title = null,
                mapper = null,
                mapperId = null,
                hideGraved = false,
                hideRanked = false,
                countTotal = false,
                asStatistics = true,
                statisticsStart = start.epochSecond,
                statisticsEnd = end.epochSecond,
                limit = null,
                page = null
        )

        val beatmaps = beatmapDataSource.findAll(filter)

        val uniqueNominators = beatmaps.response.map { it.nominators }.flatten().distinct().toMutableList()
        uniqueNominators.remove(0)

        val allStatistics = mutableListOf<UserStatistics>()

        for (nominator in uniqueNominators) {
            val nominatorBeatmaps = beatmaps.response.filter { it.nominators.contains(nominator) }
            val uniquePairNominators = nominatorBeatmaps.map { it.nominators }.flatten().distinct()
                    .toMutableList()
            // Remove the current nominator we are checking
            uniquePairNominators.remove(nominator)

            // Remove the default 0 nominator
            uniquePairNominators.remove(0)

            val pairBeatmaps = uniquePairNominators.map { pairNominator ->
                val pairNominatorBeatmaps = nominatorBeatmaps.filter { it.nominators.contains(pairNominator) }

                StatisticsIconPair(
                        otherNominatorOsuId = pairNominator,

                        total = pairNominatorBeatmaps.count(),
                        totalPopped = pairNominatorBeatmaps.filter { it.status == BeatmapStatus.Popped.prio }.count(),
                        totalDisqualified = pairNominatorBeatmaps.filter { it.status == BeatmapStatus.Disqualified.prio }.count(),
                        totalRanked = pairNominatorBeatmaps.filter { it.status == BeatmapStatus.Ranked.prio }.count(),
                        totalGraved = pairNominatorBeatmaps.filter { it.status == BeatmapStatus.Graved.prio }.count()
                )
            }

            val mapperIcons = nominatorBeatmaps.groupBy { it.mapperId }.map {
                (mapper, beatmaps) -> StatisticsMapperIcon(mapper, beatmaps.count())
            }

            val statistics = UserStatistics(
                    _id = null,

                    osuId = nominator,
                    timestamp = now.epochSecond,
                    statisticsType = statisticsType,

                    iconPairs = pairBeatmaps,
                    mapperIcons = mapperIcons,

                    totalPending = nominatorBeatmaps.filter { it.status == BeatmapStatus.Pending.prio }.count(),
                    totalUnfinished = nominatorBeatmaps.filter { it.status == BeatmapStatus.Unfinished.prio }.count(),
                    totalNominated = nominatorBeatmaps.filter { it.status == BeatmapStatus.Qualified.prio }.count(),
                    totalBubbled = nominatorBeatmaps.filter { it.status == BeatmapStatus.Bubbled.prio }.count(),
                    totalDisqualified = nominatorBeatmaps.filter { it.status == BeatmapStatus.Disqualified.prio }.count(),
                    totalPopped = nominatorBeatmaps.filter { it.status == BeatmapStatus.Popped.prio }.count(),
                    totalRanked = nominatorBeatmaps.filter { it.status == BeatmapStatus.Ranked.prio }.count(),
                    totalGraved = nominatorBeatmaps.filter { it.status == BeatmapStatus.Graved.prio }.count(),
                    totalIcons = nominatorBeatmaps.count()
            )

            allStatistics.add(statistics)
        }

        return allStatistics
    }

    @Scheduled(cron = "0 0 1 * * *")
    fun calculateMonthlyUserStatistics() {
        val currentMonth = YearMonth.now()
        val lastMonth = currentMonth.minusMonths(1)

        val startMonth = lastMonth.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC)
        val endMonth = currentMonth.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC)

        val statistics = generateUserStatistics(startMonth, endMonth, StatisticsType.MONTHLY, startMonth)

        userStatisticsDataSource.saveAll(statistics)
    }

    @Scheduled(cron = "0 0 1 1 * *")
    fun calculateYearlyUserStatistics() {
        val currentYear = YearMonth.now()
        val lastYear = currentYear.minusYears(1)
        val startLastYear = lastYear.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC)
        val endLastYear = currentYear.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC)

        val statistics = generateUserStatistics(startLastYear, endLastYear, StatisticsType.YEARLY, startLastYear)
        for (i in 1..12) { //TODO remove this, was used to generate test data
            val monthStart = lastYear.withMonth(i)
            val monthEnd = monthStart.plusMonths(1)
            val instantMonthStart = monthStart.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC)

            val monthlyStatistics = generateUserStatistics(
                    instantMonthStart,
                    monthEnd.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC),
                    StatisticsType.MONTHLY,
                    instantMonthStart
            )

            userStatisticsDataSource.saveAll(monthlyStatistics)
        }

        userStatisticsDataSource.saveAll(statistics)
    }

    @Scheduled(cron = "0 0 * * * *")
    fun calculateDailyUserStatistics() {
        val now = Instant.now().truncatedTo(ChronoUnit.DAYS)
        val startYesterday = now.minus(1, ChronoUnit.DAYS)
        val endYesterday = now

        val statistics = generateUserStatistics(startYesterday, endYesterday, StatisticsType.DAILY, startYesterday)

        userStatisticsDataSource.saveAll(statistics)
    }

    // Execute every day
    @Scheduled(cron = "0 0 * * * *")
    fun calculateDailyStatistics() {
        val filter = BeatmapFilter(
                artist = null,
                title = null,
                mapper = null,
                mapperId = null,
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

                    if (user != null && (user.role !== OsuRole.OBS && user.role !== OsuRole.GST)) {
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
                activeUnfinished = beatmapsValue.count { it.status == BeatmapStatus.Unfinished.prio },
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

        statisticsDataSource.save(statistics)
    }

}