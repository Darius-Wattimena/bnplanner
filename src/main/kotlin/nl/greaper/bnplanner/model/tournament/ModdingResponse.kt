package nl.greaper.bnplanner.model.tournament

import org.bson.codecs.pojo.annotations.BsonId

data class ModdingResponse(
        @BsonId
        val _id: String,
        val moddingCommentId: String,
        val authorOsuId: Long,
        val content: String
)