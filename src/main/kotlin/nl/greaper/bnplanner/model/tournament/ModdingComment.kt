package nl.greaper.bnplanner.model.tournament

import org.bson.codecs.pojo.annotations.BsonId

data class ModdingComment(
        @BsonId
        val _id: String,
        val moddingMapId: String,
        val authorOsuId: Long,
        val osuTimestamp: String,
        val content: String,
        val resolved: Boolean
)