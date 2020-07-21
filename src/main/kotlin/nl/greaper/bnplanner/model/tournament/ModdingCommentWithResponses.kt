package nl.greaper.bnplanner.model.tournament

data class ModdingCommentWithResponses(
        val moddingComment: ModdingComment,
        val moddingResponses: List<ModdingResponse>

)