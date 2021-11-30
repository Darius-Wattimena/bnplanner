package nl.greaper.bnplanner.model

import nl.greaper.bnplanner.util.copyableRandomUUID

data class LegacyFindResponse<T>(
        val total: Int = 0,
        val count: Int = 0,
        val response: List<T> = emptyList(),
        val uuid: String = copyableRandomUUID()
)