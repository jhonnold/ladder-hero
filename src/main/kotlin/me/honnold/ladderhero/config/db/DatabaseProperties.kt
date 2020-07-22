package me.honnold.ladderhero.config.db

import org.springframework.boot.autoconfigure.flyway.FlywayProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class DatabaseProperties {
    @Bean("flywayProperties")
    open fun getFlywayProperties(): FlywayProperties {
        return FlywayProperties()
    }
}