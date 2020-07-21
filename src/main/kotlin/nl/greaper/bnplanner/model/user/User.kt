package nl.greaper.bnplanner.model.user

import nl.greaper.bnplanner.model.event.Event
import org.bson.codecs.pojo.annotations.BsonId

data class User(
        @BsonId
        val osuId: Long,
        var osuName: String,
        var profilePictureUri: String,
        var aliases: MutableList<String> = mutableListOf(),
        var hasEditPermissions: Boolean = false,
        var hasAdminPermissions: Boolean = false,
        var hasHiddenPermissions: Boolean = false,
        var authId: String? = null,
        var role: OsuRole = OsuRole.OBS,
        val plannerEvents: MutableList<Event> = mutableListOf(),
        var lastToken: String = "",
        var lastTokenExpire: Long = 0
)

enum class OsuRole {
        BN,
        PBN,
        NAT,
        CA,
        OBS
}