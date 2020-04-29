package nl.greaper.bnplanner.model.auth

import nl.greaper.bnplanner.util.copyableRandomUUID
import org.bson.codecs.pojo.annotations.BsonId

data class AuthUser(
        val username: String,
        val password: String,

        @BsonId
        val _id: String = copyableRandomUUID()
)