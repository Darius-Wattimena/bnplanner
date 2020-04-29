package nl.greaper.bnplanner.model.user

data class UpdatedUser(
        val osuName: String,
        val role: OsuRole,
        val hasAdminPermissions: Boolean,
        val hasEditPermissions: Boolean
)