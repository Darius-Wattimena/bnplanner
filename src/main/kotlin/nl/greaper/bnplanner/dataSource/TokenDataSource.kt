package nl.greaper.bnplanner.dataSource

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import nl.greaper.bnplanner.model.auth.AuthToken
import nl.greaper.bnplanner.model.auth.AuthTokens
import org.litote.kmongo.*
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

@Component
class TokenDataSource(val database: MongoDatabase) {
    fun getCollection(): MongoCollection<AuthTokens> {
        return database.getCollection()
    }

    fun addToken(username: String, token: String, expires: Date) {
        val collection = getCollection()
        var userAuthTokens = collection.findOne(AuthTokens::username eq username)

        if (userAuthTokens == null) {
            userAuthTokens = AuthTokens(username, mutableListOf())
        }

        if (!userAuthTokens.tokens.any { it.token == token }) {
            userAuthTokens.tokens.add(AuthToken(token, expires))

            collection.save(userAuthTokens)
        }
    }
}