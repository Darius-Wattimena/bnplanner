package nl.greaper.bnplanner.model.beatmap

import nl.greaper.bnplanner.model.event.Event

data class DetailedBeatmap(
        val osuId: Long,
        val artist: String,
        val title: String,
        val note: String,
        val mapper: String,
        val mapperId: Long,
        val status: Long,
        val nominators: List<Long>,
        val interested: MutableList<Long>,
        val plannerEvents: List<Event>,
        val osuEvents: List<Event>,
        val nominatedByBNOne: Boolean = false,
        val nominatedByBNTwo: Boolean = false,
        val dateUpdated: Long,
        val unfinished: Boolean = false
)