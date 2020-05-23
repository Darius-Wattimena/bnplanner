package nl.greaper.bnplanner.model.user

data class FoundUser(
        val osuId: Long,
        val osuName: String,
        val aliases: List<String>?,
        val profilePictureUri: String?,
        val hasEditPermissions: Boolean,
        val hasAdminPermissions: Boolean,
        val role: OsuRole
)