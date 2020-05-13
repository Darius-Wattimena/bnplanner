package nl.greaper.bnplanner.service

import com.natpryce.konfig.Configuration
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import nl.greaper.bnplanner.config.KonfigConfiguration
import nl.greaper.bnplanner.dataSource.AuthDataSource
import nl.greaper.bnplanner.dataSource.TokenDataSource
import nl.greaper.bnplanner.exception.RegistrationException
import nl.greaper.bnplanner.exception.UnauthorizedException
import nl.greaper.bnplanner.model.auth.AuthUser
import nl.greaper.bnplanner.model.auth.AuthenticationResponse
import nl.greaper.bnplanner.model.auth.Credentials
import nl.greaper.bnplanner.model.user.User
import nl.greaper.bnplanner.util.EncriptionUtil
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import javax.crypto.SecretKey

@Service
class AuthService(
        val config: Configuration,
        val dataSource: AuthDataSource,
        val tokenDataSource: TokenDataSource,
        val userService: UserService
) {
    private val secretKey = getSecretString()

    fun register(credentials: Credentials): AuthenticationResponse {
        val foundUser = dataSource.find(credentials.username)
        if (foundUser == null) {
            val encryptedPassword = EncriptionUtil.aesEncrypt(credentials.password, secretKey)
            val newUser = AuthUser(credentials.username, encryptedPassword)
            dataSource.create(newUser)
            val token = generateAndSaveToken(newUser, null)

            return AuthenticationResponse(
                    username = credentials.username,
                    token = token
            )
        } else {
            throw RegistrationException("Username already taken")
        }
    }

    fun performLogin(credentials: Credentials) : AuthenticationResponse {
        val foundUser = dataSource.find(credentials.username)
        if (foundUser != null) {
            val decryptedPassword = EncriptionUtil.aesDecrypt(foundUser.password, secretKey)

            if (credentials.password == decryptedPassword) {
                val userProfile = userService.findUserWithAuth(foundUser._id)

                val token = if (userProfile != null) {
                    generateAndSaveToken(foundUser, userProfile)
                } else {
                    throw UnauthorizedException("No user profile is bound to your account, contact Greaper before logging in again!")
                }

                return AuthenticationResponse(
                        username = foundUser.username,
                        token = token
                )
            }
        }

        throw UnauthorizedException("An error occurred while authorizing!")
    }

    fun getUserFromAuthToken(authToken: String): User {
        val secretKey = getSecretKey()
        val claims = Jwts.parserBuilder()
                .setSigningKey(secretKey).build()
                .parseClaimsJws(authToken.removePrefix("Bearer ")).body

        return userService.findUser((claims["osuId"] as Int).toLong())
    }

    private fun generateAndSaveToken(authUser: AuthUser, user: User?): String {
        val currentDate = Instant.now()
        val expireDate = Date.from(currentDate.plus(2L * 7L, ChronoUnit.DAYS)) // Expires in 2 weeks

        val secretKey = getSecretKey()
        val jwt = Jwts.builder()
                .setSubject("Users/${authUser.username}")
                .claim("username", authUser.username)

        if (user != null) {
            jwt.claim("isAdmin", user.hasAdminPermissions)
                    .claim("canEdit", user.hasEditPermissions)
                    .claim("osuName", user.osuName)
                    .claim("role", user.role)
                    .claim("osuId", user.osuId)
        }

        val token = jwt.setIssuedAt(Date.from(currentDate))
                .setExpiration(expireDate)
                .signWith(secretKey)
                .compact()


        tokenDataSource.addToken(authUser.username, token, expireDate)

        return token
    }

    private fun getSecretString() = config[KonfigConfiguration.auth.secret]

    private fun getSecretKey(secretString: String = getSecretString()): SecretKey? {
        return Keys.hmacShaKeyFor(secretString.toByteArray(StandardCharsets.UTF_8))
    }

}