package me.honnold.ladderhero.service

import me.honnold.ladderhero.dao.UserDAO
import me.honnold.ladderhero.dao.domain.User
import me.honnold.ladderhero.service.dto.SC2Profile
import org.json.simple.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.net.URI
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

@Service
class BlizzardService(
    private val userDAO: UserDAO,
    private val blizzardClientId: String,
    @Qualifier("blizzardClientSecret")
    private val blizzardClientSecret: String,
    private val blizzardRedirectUri: URI
) {
    companion object {
        private val logger = LoggerFactory.getLogger(BlizzardService::class.java)
    }

    private val client = WebClient.create()
    private var currentAccessToken = ""
    private var accessTokenExpiration: Long = 0

    fun getAuthorizeRedirectUri(username: String): Mono<URI> {
        logger.trace("Generating authorize link for $username!")

        return userDAO.findByUsername(username).map { user ->
            UriComponentsBuilder.fromUriString("https://us.battle.net/oauth/authorize")
                .queryParam("client_id", blizzardClientId)
                .queryParam("scope", "sc2.profile")
                .queryParam("state", user.id)
                .queryParam("redirect_uri", blizzardRedirectUri)
                .queryParam("response_type", "code")
                .buildAndExpand()
                .toUri()
        }
    }

    fun associateBlizzardCodeToUser(code: String, userId: UUID): Mono<User> {
        val body = LinkedMultiValueMap<String, String>()
        body["redirect_uri"] = blizzardRedirectUri.toString()
        body["scope"] = "sc2.profile"
        body["grant_type"] = "authorization_code"
        body["code"] = code

        return client.post()
            .uri("https://us.battle.net/oauth/token")
            .headers { h -> h.setBasicAuth(blizzardClientId, blizzardClientSecret) }
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(body))
            .retrieve()
            .bodyToMono(JSONObject::class.java)
            .doOnSuccess { logger.info("Fetching access token for $userId") }
            .map { res -> res["access_token"] as String }
            .flatMap { token ->
                client.get()
                    .uri("https://us.battle.net/oauth/userinfo")
                    .headers { h -> h.setBearerAuth(token) }
                    .retrieve()
                    .bodyToMono(JSONObject::class.java)
                    .doOnSuccess { logger.info("Fetching user info for $userId") }
            }
            .flatMap { res ->
                val battleTag = res["battletag"] as String
                val id = res["id"] as Int

                userDAO.findById(userId)
                    .flatMap { user ->
                        user.battleTag = battleTag
                        user.accountId = id.toLong()

                        userDAO.save(user)
                    }
            }
    }

    fun getSc2PlayerInfo(accountId: Long): Mono<List<SC2Profile.SC2Account>> {
        return refreshClientAccess().flatMap { token ->
            client.get()
                .uri("https://us.api.blizzard.com/sc2/player/$accountId")
                .headers { h -> h.setBearerAuth(token) }
                .retrieve()
                .bodyToMono(object : ParameterizedTypeReference<List<SC2Profile.SC2Account>>() {})
                .doOnSuccess { logger.info("Successfully fetched SC2 Player Info for $accountId") }
        }
    }

    private fun refreshClientAccess(): Mono<String> {
        val current = LocalDateTime.now()
        val timeInSeconds = current.toEpochSecond(ZoneOffset.UTC)

        if (timeInSeconds < accessTokenExpiration) return Mono.just(this.currentAccessToken)

        logger.info("Current Blizzard client token is expired, refreshing for another!")

        val body = LinkedMultiValueMap<String, String>()
        body["grant_type"] = "client_credentials"

        return client.post()
            .uri("https://us.battle.net/oauth/token")
            .headers { h -> h.setBasicAuth(blizzardClientId, blizzardClientSecret) }
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(body))
            .retrieve()
            .bodyToMono(JSONObject::class.java)
            .map { res ->
                val newToken = res["access_token"] as String
                val tokenDuration = res["expires_in"] as Int

                this.currentAccessToken = newToken
                this.accessTokenExpiration += (timeInSeconds + tokenDuration - 3600)

                this.currentAccessToken
            }
    }
}
