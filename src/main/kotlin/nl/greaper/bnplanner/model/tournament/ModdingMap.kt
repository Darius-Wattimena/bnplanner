package nl.greaper.bnplanner.model.tournament

import org.bson.codecs.pojo.annotations.BsonId

data class ModdingMap(
        @BsonId
        val _id: String,
        val contestId: String,
        val artist: String,
        val title: String,
        val downloadLink: String
)