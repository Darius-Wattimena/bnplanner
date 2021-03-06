package nl.greaper.bnplanner.model

import org.bson.codecs.pojo.annotations.BsonId

data class Statistics(
        @BsonId
        val _id: String?,

        val totalUser: Int,
        val totalNominators: Int,
        val totalFullNominators: Int,
        val totalProbation: Int,
        val totalNATs: Int,
        val totalOtherNominators: Int,
        val totalGuests: Int,

        val activePending: Int,
        val activeUnfinished: Int,
        val activeNominated: Int,
        val activeBubbled: Int,
        val activeDisqualified: Int,
        val activePopped: Int,

        val totalBeatmaps: Int,
        val totalInProgress: Int,
        val totalMissingSecondBN: Int,

        val userPendingIcons: Map<Long, Int>,
        val userNominatedIcons: Map<Long, Int>,

        val timestamp: Long
)