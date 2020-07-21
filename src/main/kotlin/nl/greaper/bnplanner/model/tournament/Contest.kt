package nl.greaper.bnplanner.model.tournament

import org.bson.codecs.pojo.annotations.BsonId

data class Contest(
        @BsonId
        val _id: String,
        val name: String,
        val accessIds: List<Long>
)