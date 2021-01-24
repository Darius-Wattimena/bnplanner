package nl.greaper.bnplanner.model

import org.bson.codecs.pojo.annotations.BsonId

data class UserStatistics(
        @BsonId
        val _id: String?,

        val osuId: Long,
        val timestamp: Long,
        val statisticsType: StatisticsType,

        val iconPairs: List<StatisticsIconPair>,
        val mapperIcons: List<StatisticsMapperIcon>,

        val totalPending: Int,
        val totalUnfinished: Int,
        val totalNominated: Int,
        val totalBubbled: Int,
        val totalDisqualified: Int,
        val totalPopped: Int,
        val totalRanked: Int,
        val totalGraved: Int
) {
    enum class StatisticsType {
        DAILY,
        MONTHLY,
        YEARLY
    }

    data class StatisticsMapperIcon(
            val mapperId: Long,
            val total: Int
    )

    data class StatisticsIconPair(
            val otherNominatorOsuId: Long,

            val total: Int,
            val totalPopped: Int,
            val totalDisqualified: Int,
            val totalRanked: Int,
            val totalGraved: Int
    )
}