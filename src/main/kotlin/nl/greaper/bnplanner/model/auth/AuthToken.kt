package nl.greaper.bnplanner.model.auth

import java.util.*

data class AuthToken(
        val token: String,
        val expires: Date
)