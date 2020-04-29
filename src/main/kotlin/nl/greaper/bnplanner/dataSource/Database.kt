package nl.greaper.bnplanner.dataSource

import com.mongodb.MongoClientURI
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.client.MongoDatabase
import com.natpryce.konfig.Configuration
import nl.greaper.bnplanner.config.KonfigConfiguration.mongodb
import org.litote.kmongo.KMongo
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class Database(val config: Configuration) {
    @Bean
    fun databaseInstance(): MongoDatabase {
        val username = config[mongodb.username]
        val db = config[mongodb.db]
        val password = config[mongodb.password]
        val host = config[mongodb.host]

        val client = if (password != "") {
            // Live mongo
            KMongo.createClient(MongoClientURI("mongodb+srv://$username:$password@$host/$db?retryWrites=true&w=majority"))
        } else {
            // Local mongo
            KMongo.createClient(MongoClientURI("mongodb://$host/$db"))
        }
        return client.getDatabase(db)
    }
}