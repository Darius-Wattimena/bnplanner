package nl.greaper.bnplanner.util

import nl.greaper.bnplanner.model.user.OsuRole

fun OsuRole.getReadableName(): String {
    return when(this) {
        OsuRole.BN -> "Beatmap Nominator"
        OsuRole.PBN -> "Probation Beatmap Nominator"
        OsuRole.NAT -> "Nomination Assessment Team"
        OsuRole.CA -> "Retired Nominator"
        OsuRole.OBS -> "Other Nominator"
        OsuRole.GST -> "Guest"
    }
}