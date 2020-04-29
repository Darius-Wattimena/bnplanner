package nl.greaper.bnplanner.controller

import nl.greaper.bnplanner.model.auth.Credentials
import nl.greaper.bnplanner.service.AuthService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/auth")
class AuthController(val service: AuthService) {
    @PostMapping("/login")
    fun login(@RequestBody credentials: Credentials) = service.performLogin(credentials)

    @PostMapping("/register")
    fun register(@RequestBody credentials: Credentials) = service.register(credentials)
}