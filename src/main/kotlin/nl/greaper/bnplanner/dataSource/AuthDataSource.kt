package nl.greaper.bnplanner.dataSource

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import nl.greaper.bnplanner.model.auth.AuthUser
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.springframework.stereotype.Component

@Component
class AuthDataSource(val database: MongoDatabase) {
    fun getCollection(): MongoCollection<AuthUser> = database.getCollection()

    fun find(username: String) : AuthUser? =
            getCollection().findOne(AuthUser::username eq username)

    fun create(user: AuthUser) = getCollection().insertOne(user)
}