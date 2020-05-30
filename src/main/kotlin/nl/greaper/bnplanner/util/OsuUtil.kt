package nl.greaper.bnplanner.util

import nl.greaper.bnplanner.model.osu.GroupBadge
import nl.greaper.bnplanner.model.user.OsuRole
import nl.greaper.bnplanner.model.user.User

fun getUserRole(user: User, groups: List<GroupBadge>): OsuRole {
    if (groups.isNotEmpty()) {
        //TODO move those to a config?
        val probationHybridBN = arrayOf(4236057L)

        return if (groups.any { it.id == GroupBadge.NAT }) {
            OsuRole.NAT
        } else if (groups.any { it.id == GroupBadge.PBN }) {
            OsuRole.PBN
        } else if (groups.any { it.id == GroupBadge.BN }) {
            if (probationHybridBN.any { it == user.osuId }) {
                OsuRole.PBN
            } else {
                OsuRole.BN
            }
        } else {
            if (user.role != OsuRole.OBS) {
                OsuRole.CA
            } else {
                OsuRole.OBS
            }
        }
    } else {
        return if (user.role != OsuRole.OBS) {
            OsuRole.CA
        } else {
            OsuRole.OBS
        }
    }
}