package me.honnold.ladderhero.web

import me.honnold.ladderhero.service.AuthService
import me.honnold.ladderhero.web.request.AuthRequest
import me.honnold.ladderhero.service.dto.JWTToken
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService) {
    companion object {
        private val logger = LoggerFactory.getLogger(AuthController::class.java)
    }

    @PostMapping(path = ["/register"])
    fun register(@RequestBody body: AuthRequest): Mono<JWTToken> {
        return this.authService.registerUser(body)
    }
}