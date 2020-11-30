package nl.greaper.bnplanner.model.auth

data class OsuOAuth(
    val client_id: Int,
    val client_secret: String,
    val code: String,
    val grant_type: String,
    val redirect_uri: String
)