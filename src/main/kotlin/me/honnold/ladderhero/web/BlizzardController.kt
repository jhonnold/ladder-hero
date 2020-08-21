package me.honnold.ladderhero.web

import java.net.URI
import java.security.Principal
import me.honnold.ladderhero.dao.UserDAO
import me.honnold.ladderhero.util.toUUID
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono

@Controller
@RequestMapping("/blizzard")
class BlizzardController(
    private val userDAO: UserDAO,
    private val blizzardClientId: String,
    private val blizzardRedirectUri: URI,
    @Qualifier("homePageUri")
    private val homePageUri: URI
) {
    companion object {
        private val logger = LoggerFactory.getLogger(BlizzardController::class.java)
    }

    @GetMapping(path = ["/authorize"])
    fun authorizeBlizzard(principal: Principal, response: ServerHttpResponse): Mono<Void> {
        return userDAO.findByUsername(principal.name).flatMap { user ->
            response.statusCode = HttpStatus.TEMPORARY_REDIRECT
            response.headers.location =
                UriComponentsBuilder.fromUriString("https://us.battle.net/oauth/authorize")
                    .queryParam("client_id", blizzardClientId)
                    .queryParam("scope", "sc2.profile")
                    .queryParam("state", user.id)
                    .queryParam("redirect_uri", blizzardRedirectUri)
                    .queryParam("response_type", "code")
                    .buildAndExpand()
                    .toUri()

            response.setComplete()
        }
    }

    @GetMapping(path = ["/code"])
    fun code(
        @RequestParam code: String, @RequestParam state: String, response: ServerHttpResponse
    ): Mono<Void> {
        return userDAO
            .findById(state.toUUID())
            .flatMap { user ->
                user.code = code

                userDAO.save(user)
            }
            .flatMap {
                response.statusCode = HttpStatus.TEMPORARY_REDIRECT
                response.headers.location = homePageUri

                response.setComplete()
            }
    }
}
