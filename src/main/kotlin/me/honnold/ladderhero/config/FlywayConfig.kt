package me.honnold.ladderhero.config

import org.flywaydb.core.Flyway
import org.springframework.boot.autoconfigure.flyway.FlywayProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class FlywayConfig {
    @Bean("flywayProperties")
    open fun getFlywayProperties(): FlywayProperties {
        return FlywayProperties()
    }

    @Bean(initMethod = "migrate")
    open fun flyway(): Flyway? {
        val flywayProperties = getFlywayProperties()

        return Flyway(
            Flyway.configure().baselineOnMigrate(true)
                .dataSource(flywayProperties.url, flywayProperties.user, flywayProperties.password)
        )
    }
}