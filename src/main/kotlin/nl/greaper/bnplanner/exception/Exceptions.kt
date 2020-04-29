package nl.greaper.bnplanner.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
class RegistrationException(message: String, cause: Throwable? = null): RuntimeException(message, cause)

@ResponseStatus(code = HttpStatus.UNAUTHORIZED)
class UnauthorizedException(message: String, cause: Throwable? = null): RuntimeException(message, cause)

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
class BeatmapException(message: String, cause: Throwable? = null): RuntimeException(message, cause)

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
class UserException(message: String, cause: Throwable? = null): RuntimeException(message, cause)