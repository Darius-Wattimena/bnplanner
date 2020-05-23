package nl.greaper.bnplanner.model.osu

/**
 * id
 * identifier
 * name
 * short_name
 * description
 * colour
 */
data class GroupBadge(
        val id: Long
) {
    companion object {
        const val GMT = 4L
        const val NAT = 7L
        const val BN = 28L
        const val PBN = 32L
    }
}