package nl.greaper.bnplanner.model.auth

import nl.greaper.bnplanner.util.copyableRandomUUID
import org.bson.codecs.pojo.annotations.BsonId

data class AuthTokens(
        val username: String,
        val tokens: MutableList<AuthToken>,

        @BsonId
        val _id: String = copyableRandomUUID()
)