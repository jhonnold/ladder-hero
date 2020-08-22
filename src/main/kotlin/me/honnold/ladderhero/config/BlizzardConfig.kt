package me.honnold.ladderhero.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.URI

@Configuration
open class BlizzardConfig {
    @Value("\${blizzard.redirect}")
    private lateinit var blizzardRedirectUri: String

    @Value("\${blizzard.clientId}")
    private lateinit var blizzardClientId: String

    @Value("\${blizzard.clientSecret}")
    private lateinit var blizzardClientSecret: String

    @Bean("blizzardRedirectUri")
    open fun blizzardRedirectUri(): URI {
        return URI.create(blizzardRedirectUri)
    }

    @Bean("blizzardClientId")
    open fun blizzardClientId(): String {
        return blizzardClientId
    }

    @Bean("blizzardClientSecret")
    open fun blizzardClientSecret(): String {
        return blizzardClientSecret
    }
}
