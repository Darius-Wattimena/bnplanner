package nl.greaper.bnplanner.config

import com.natpryce.konfig.*
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration
class KonfigConfiguration {

    @Bean
    fun configuration() =
            systemProperties() overriding
                    EnvironmentVariables() overriding
                    ConfigurationProperties(properties = getPropertiesFromPath("application.properties"))

    private fun getPropertiesFromPath(path: String): Properties {
        val properties = Properties()
        val resource = this::class.java.classLoader.getResourceAsStream(path)
        properties.load(resource)

        return properties
    }

    object mongodb : PropertyGroup() {
        val host by stringType
        val username by stringType
        val password by stringType
        val db by stringType
    }

    object auth : PropertyGroup() {
        val secret by stringType
    }

    object cors : PropertyGroup() {
        val uris by listType(stringType)
        val methods by listType(stringType)
        val headers by listType(stringType)
    }

    object discord : PropertyGroup() {
        val webhook by stringType
        val moddingwebhook by listType(stringType)
    }
}