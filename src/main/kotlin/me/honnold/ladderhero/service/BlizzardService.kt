package me.honnold.ladderhero.service

import me.honnold.ladderhero.dao.UserDAO
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.Disposable
import reactor.core.publisher.Mono
import java.net.URI
import java.util.*

@Service
class BlizzardService(
    private val userDAO: UserDAO,
    private val blizzardClientId: String,
    private val blizzardRedirectUri: URI
) {
    companion object {
        private val logger = LoggerFactory.getLogger(BlizzardService::class.java)
    }

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

    fun associateBlizzardCodeToUser(code: String, userId: UUID): Disposable {
        return userDAO
            .findById(userId)
            .flatMap { user ->
                user.code = code

                userDAO.save(user)
            }
            .subscribe()
    }
}
