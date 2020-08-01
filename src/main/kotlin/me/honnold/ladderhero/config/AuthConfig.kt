package me.honnold.ladderhero.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
open class AuthConfig {
    @Value("\${auth.jwt.secret}")
    private lateinit var jwtSecret: String

    @Value("\${auth.jwt.expiration-duration}")
    private var expirationDuration: Long = 1800000L // 30 minutes

    @Bean("jwtSecret")
    open fun jwtSecret(): String {
        return jwtSecret
    }

    @Bean("jwtExpiration")
    open fun jwtExpiration(): Long {
        return expirationDuration
    }

    @Bean("passwordEncoder")
    open fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}