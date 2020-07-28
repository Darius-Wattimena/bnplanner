package nl.greaper.bnplanner.model.auth

data class UserProfile(
        val id: Long,
        val canEdit: Boolean = false,
        val isAdmin: Boolean = false,
        val hasHiddenPerms: Boolean = false
)