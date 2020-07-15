package nl.greaper.bnplanner.model.event

import nl.greaper.bnplanner.model.beatmap.BeatmapStatus
import nl.greaper.bnplanner.model.user.OsuRole
import nl.greaper.bnplanner.model.user.User
import nl.greaper.bnplanner.util.getReadableName
import java.time.Instant

data class Event(
        val userId: Long,
        val title: String,
        val description: String,
        val timestamp: Long = Instant.now().epochSecond
)

object Events {
    fun asBeatmapCreatedEvent(userId: Long): Event {
        return Event(userId, "Added Beatmap", "Added to the planner.")
    }

    fun asBeatmapMetadataModifiedEvent(userId: Long): Event {
        return Event(userId, "Updated Metadata", "Updated the metadata.")
    }

    fun asBeatmapNoteModifiedEvent(userId: Long): Event {
        return Event(userId, "Updated Note", "Updated the note.")
    }

    fun asBeatmapNominatorRemovedEvent(userId: Long, oldNominator: User): Event {
        return Event(userId, "Removed Nominator", "Removed ${oldNominator.osuName} as nominator.")
    }

    fun asBeatmapNominatorAddedEvent(userId: Long, nominator: User): Event {
        return Event(userId, "Added Nominator", "Added ${nominator.osuName} as nominator.")
    }

    fun asBeatmapDisqualifiedEvent(userId: Long, reason: String): Event {
        return Event(userId, "Disqualified", reason)
    }

    fun asBeatmapPoppedEvent(userId: Long, reason: String): Event {
        return Event(userId, "Popped", reason)
    }

    fun asBeatmapStatusEvent(userId: Long, newStatus: Long): Event {
        return Event(userId, "Updated Status", "Updated status to ${BeatmapStatus.fromPrio(newStatus).getReadableName()}.")
    }

    fun asUserUpdateUsernameEvent(editorId: Long, oldName: String, newName: String): Event {
        return Event(editorId, "Updated Username", "Updated username from $oldName to $newName.")
    }

    fun asUserUpdateRoleEvent(editorId: Long, role: OsuRole): Event {
        return Event(editorId, "Updated User Role", "Updated user role to ${role.getReadableName()}.")
    }

    fun asUserCreatedEvent(userId: Long): Event {
        return Event(userId, "Added User", "Added to the planner.")
    }

    fun asUserAddAdminPermissionEvent(userId: Long): Event {
        return Event(userId, "Granted Permission",  "Granted admin permissions.")
    }

    fun asUserAddEditPermissionEvent(userId: Long): Event {
        return Event(userId, "Granted Permission", "Granted edit permissions.")
    }

    fun asUserRemoveAdminPermissionEvent(userId: Long): Event {
        return Event(userId, "Revoked Permission", "Revoked admin permissions.")
    }

    fun asUserRemoveEditPermissionEvent(userId: Long): Event {
        return Event(userId, "Revoked Permission", "Revoked edit permissions.")
    }
}