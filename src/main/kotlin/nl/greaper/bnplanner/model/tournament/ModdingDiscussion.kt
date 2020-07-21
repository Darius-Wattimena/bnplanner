package nl.greaper.bnplanner.model.tournament

data class ModdingDiscussion(
        val map: ModdingMap,
        val discussions: List<ModdingCommentWithResponses>
)