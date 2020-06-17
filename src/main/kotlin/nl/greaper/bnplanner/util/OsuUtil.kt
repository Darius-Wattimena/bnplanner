package nl.greaper.bnplanner.util

import nl.greaper.bnplanner.model.osu.GroupBadge
import nl.greaper.bnplanner.model.user.OsuRole
import nl.greaper.bnplanner.model.user.User

fun getUserRole(user: User, groups: List<GroupBadge>): OsuRole {
    if (groups.isNotEmpty()) {
        return if (groups.any { it.id == GroupBadge.NAT }) {
            OsuRole.NAT
        } else if (groups.any { it.id == GroupBadge.PBN }) {
            OsuRole.PBN
        } else if (groups.any { it.id == GroupBadge.BN }) {
            OsuRole.BN
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