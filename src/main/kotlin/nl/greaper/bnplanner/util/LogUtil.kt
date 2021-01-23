package nl.greaper.bnplanner.util

import mu.KLogger

fun <T> T?.logIfNull(logger: KLogger, msg: () -> String): T? {
    if (this == null) {
        logger.info { msg.invoke() }
    }

    return this
}