package nl.greaper.bnplanner.model.filter

import nl.greaper.bnplanner.model.user.OsuRole

data class UserFilter(
        val name: String?,

        val canEdit: Boolean?,
        val isAdmin: Boolean?,

        val limit: UserFilterLimit?,
        val page: Int?,
        val countTotal: Boolean? = false,

        val roles: List<OsuRole> = emptyList()
)

enum class UserFilterLimit {
    Ten,
    Twenty;

    fun asNumber(): Int {
        return when(this) {
            Ten -> 10
            Twenty -> 20
        }
    }
}
