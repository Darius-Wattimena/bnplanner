package nl.greaper.bnplanner.dataSource

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import nl.greaper.bnplanner.config.KonfigConfiguration.mongodb
import org.litote.kmongo.service.ClassMappingType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.URLEncoder
import com.natpryce.konfig.Configuration as KotlinConfig

@Configuration
class Database() {
    private val encoding = "UTF-8"

    @Bean
    fun defaultMongoClient(config: KotlinConfig): MongoClient {
        val username = config[mongodb.username]
        val db = config[mongodb.db]
        val password = config[mongodb.password]
        val host = config[mongodb.host]

        val uri = if (password.isNotBlank()) {
            val encodedUsername = URLEncoder.encode(username, encoding)
            val encodedPassword = URLEncoder.encode(password, encoding)
            val encodedDb = URLEncoder.encode(db, encoding)
            // Live mongo
            "mongodb+srv://$encodedUsername:$encodedPassword@$host/$encodedDb?retryWrites=true&w=majority"
        } else {
            // Local mongo
            "mongodb://${config[mongodb.host]}/$db"
        }
        val settings = MongoClientSettings.builder()
            .codecRegistry(ClassMappingType.codecRegistry(MongoClientSettings.getDefaultCodecRegistry()))
            .applyConnectionString(ConnectionString(uri))
            .build()

        return MongoClients.create(settings)
    }

    @Bean
    fun defaultMongoDatabase(config: KotlinConfig, client: MongoClient): MongoDatabase {
        return client.getDatabase(config[mongodb.db])
    }
}