package me.honnold.ladderhero.web.security

import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Service
class SecurityContextRepository(private val authManager: AuthManager) :
    ServerSecurityContextRepository {
    override fun save(exchange: ServerWebExchange, context: SecurityContext): Mono<Void> {
        TODO("Not yet implemented")
    }

    override fun load(exchange: ServerWebExchange): Mono<SecurityContext> {
        val authHeader = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)

        if (authHeader == null || authHeader.isEmpty() || !authHeader.startsWith("Bearer"))
            return Mono.empty()

        val token = authHeader.substring(7)
        return this.authManager.authenticate(UsernamePasswordAuthenticationToken(token, token))
            .map { auth -> SecurityContextImpl(auth) }
    }
}
