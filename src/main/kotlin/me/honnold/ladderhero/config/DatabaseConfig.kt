package me.honnold.ladderhero.config

import org.springframework.boot.autoconfigure.flyway.FlywayProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class DatabaseConfig {
    @Bean("flywayProperties")
    open fun getFlywayProperties(): FlywayProperties {
        return FlywayProperties()
    }
}