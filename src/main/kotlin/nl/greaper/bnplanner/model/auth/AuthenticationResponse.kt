package nl.greaper.bnplanner.model.auth

data class AuthenticationResponse(
        val username: String,
        val token: String
)