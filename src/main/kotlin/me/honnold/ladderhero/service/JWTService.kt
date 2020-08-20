package me.honnold.ladderhero.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.security.Key
import java.util.*
import javax.annotation.PostConstruct
import me.honnold.ladderhero.dao.domain.User
import me.honnold.ladderhero.service.dto.JWTToken
import org.springframework.stereotype.Service

@Service
class JWTService(private val jwtSecret: String, private val jwtExpiration: Long) {
    private lateinit var signingKey: Key

    @PostConstruct
    fun init() {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }

    fun getJWTToken(user: User): JWTToken {
        val token = this.generateToken(emptyMap(), user.username)

        return JWTToken(token)
    }

    fun getClaimsFromToken(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(this.signingKey)
            .build()
            .parseClaimsJws(token)
            .body
    }

    private fun generateToken(claims: Map<String, Any>, subject: String): String {
        val now = Date()
        val expirationDate = Date(now.time + jwtExpiration)

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(now)
            .setExpiration(expirationDate)
            .signWith(this.signingKey)
            .compact()
    }
}
