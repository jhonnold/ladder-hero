package me.honnold.ladderhero.web.security

import me.honnold.ladderhero.service.JWTService
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class AuthManager(private val jwtService: JWTService) : ReactiveAuthenticationManager {
    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        return Mono.just(authentication.credentials.toString())
            .map { jwtService.getClaimsFromToken(it) }
            .map { claims ->
                UsernamePasswordAuthenticationToken(
                    claims.subject, null, emptyList()
                ) as Authentication
            }
            .onErrorResume { Mono.empty() }
    }
}
