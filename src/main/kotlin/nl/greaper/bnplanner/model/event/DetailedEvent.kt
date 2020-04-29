package nl.greaper.bnplanner.model.event

import nl.greaper.bnplanner.model.user.User

data class DetailedEvent(
        val user: User,
        val title: String,
        val description: String,
        val timestamp: Long
)