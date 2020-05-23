package nl.greaper.bnplanner.util

import nl.greaper.bnplanner.model.osu.GroupBadge
import nl.greaper.bnplanner.model.user.OsuRole
import nl.greaper.bnplanner.model.user.User

fun getUserRole(user: User, group: GroupBadge?): OsuRole {
    return if (group != null) {
        //TODO move those to a config?
        val natGmt = arrayOf(318565L)
        val probationGMT = arrayOf<Long>()
        val fullGMT = arrayOf<Long>()
        val probationHybridBN = arrayOf(4236057L)

        when (group.id) {
            GroupBadge.BN -> {
                if (probationHybridBN.any { it == user.osuId }) {
                    OsuRole.PBN
                } else {
                    OsuRole.BN
                }
            }
            GroupBadge.PBN -> {
                OsuRole.PBN
            }
            GroupBadge.NAT -> {
                OsuRole.NAT
            }
            GroupBadge.GMT -> {
                if (natGmt.any { it == user.osuId }) {
                    OsuRole.NAT
                } else if (fullGMT.any { it == user.osuId }) {
                    OsuRole.BN
                } else if (probationGMT.any { it == user.osuId }) {
                    OsuRole.PBN
                } else {
                    if (user.role != OsuRole.OBS) {
                        OsuRole.CA
                    } else {
                        OsuRole.OBS
                    }
                }
            }
            else -> {
                if (user.role != OsuRole.OBS) {
                    OsuRole.CA
                } else {
                    OsuRole.OBS
                }
            }
        }
    } else {
        if (user.role != OsuRole.OBS) {
            OsuRole.CA
        } else {
            OsuRole.OBS
        }
    }
}