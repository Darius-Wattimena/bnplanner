package nl.greaper.bnplanner.model.beatmap

import nl.greaper.bnplanner.model.event.Event

data class DetailedBeatmap (
        val osuId: Long,
        var artist: String,
        var title: String,
        var note: String,
        var mapper: String,
        var status: Long,
        var nominators: MutableList<Long>,
        val interested: MutableList<Long>,
        val events: List<Event>,
        val nominatedByBNOne: Boolean = false,
        val nominatedByBNTwo: Boolean = false
)