package me.honnold.ladderhero.service

import me.honnold.ladderhero.service.domain.UserService
import me.honnold.ladderhero.service.dto.JWTToken
import me.honnold.ladderhero.web.request.AuthRequest
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class AuthService(
    private val passwordEncoder: PasswordEncoder,
    private val userService: UserService,
    private val jwtService: JWTService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(AuthService::class.java)
    }

    fun registerUser(body: AuthRequest): Mono<JWTToken> {
        val encodedPassword = passwordEncoder.encode(body.password)

        return this.userService.createUser(body.username, encodedPassword)
            .map { user -> this.jwtService.getJWTToken(user) }
    }
}