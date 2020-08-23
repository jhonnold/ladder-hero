package me.honnold.ladderhero.web

import me.honnold.ladderhero.service.BlizzardService
import me.honnold.ladderhero.util.toUUID
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import reactor.core.publisher.Mono
import java.net.URI
import java.security.Principal

@Controller
@RequestMapping("/blizzard")
class BlizzardController(
    private val blizzardService: BlizzardService,
    @Qualifier("homePageUri")
    private val homePageUri: URI
) {
    companion object {
        private val logger = LoggerFactory.getLogger(BlizzardController::class.java)
    }

    @GetMapping(path = ["/authorize"])
    fun authorizeBlizzard(principal: Principal, response: ServerHttpResponse): Mono<Void> {
        return blizzardService.getAuthorizeRedirectUri(principal.name).flatMap { uri ->
            response.statusCode = HttpStatus.FOUND
            response.headers.location = uri
            response.setComplete()
        }
    }

    @GetMapping(path = ["/code"])
    fun code(
        @RequestParam code: String,
        @RequestParam state: String,
        response: ServerHttpResponse
    ): Mono<Void> {
        return blizzardService.associateBlizzardCodeToUser(code, state.toUUID())
            .flatMap {
                response.statusCode = HttpStatus.FOUND
                response.headers.location = homePageUri
                response.setComplete()
            }
    }
}
