package me.honnold.ladderhero.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
open class SecurityConfig {
    @Value("\${admin.user}")
    lateinit var adminUser: String

    @Value("\${admin.password}")
    lateinit var adminPassword: String

    @Bean
    open fun securityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf().disable()
            .authorizeExchange().anyExchange().authenticated()
            .and().httpBasic()
            .and().formLogin().disable()
            .build()
    }

    @Bean
    open fun userDetailsService(): MapReactiveUserDetailsService {
        val admin = User.builder()
            .username(adminUser)
            .password(adminPassword)
            .roles("ADMIN")
            .build()

        return MapReactiveUserDetailsService(admin)
    }
}