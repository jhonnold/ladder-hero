package me.honnold.ladderhero.config

import java.net.URI
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class AppConfig {
    @Value("\${app.home-page}")
    private lateinit var homePage: String

    @Bean("homePageUri")
    open fun homePageUri(): URI {
        return URI.create(homePage)
    }
}
