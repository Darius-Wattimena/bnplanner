package nl.greaper.bnplanner.model.user

import nl.greaper.bnplanner.model.event.DetailedEvent

data class DetailedUser(
        val osuId: Long,
        var osuName: String,
        var profilePictureUri: String,
        var aliases: MutableList<String>,
        val hasEditPermissions: Boolean,
        val hasAdminPermissions: Boolean,
        var role: OsuRole,
        val events: List<DetailedEvent>
)