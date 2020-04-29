package nl.greaper.bnplanner.util

import java.util.*

fun copyableRandomUUID() : String {
    return UUID.randomUUID().toString().replace("-".toRegex(), "")
}