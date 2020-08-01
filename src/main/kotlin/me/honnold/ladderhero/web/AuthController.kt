package me.honnold.ladderhero.web

import me.honnold.ladderhero.exception.UsernameAlreadyTakenException
import me.honnold.ladderhero.service.AuthService
import me.honnold.ladderhero.web.request.AuthRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService) {
    companion object {
        private val logger = LoggerFactory.getLogger(AuthController::class.java)
    }

    @PostMapping(path = ["/register"])
    fun register(@RequestBody body: AuthRequest): Mono<ResponseEntity<Any>> {
        return this.authService.registerUser(body)
            .map { token -> ResponseEntity.ok<Any>(token) }
            .doOnSuccess { logger.info("Successfully registered new user ${body.username}!") }
            .doOnError { t -> logger.error(t.message) }
            .onErrorResume { t ->
                Mono.just<ResponseEntity<Any>>(
                    when (t) {
                        is UsernameAlreadyTakenException -> ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                            .body<Any>(t.message)
                        else -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body<Any>("An unknown error occurred!")
                    }
                )
            }
    }

    @PostMapping(path = ["/login"])
    fun login(@RequestBody body: AuthRequest): Mono<ResponseEntity<Any>> {
        return this.authService.login(body)
            .map { token -> ResponseEntity.ok<Any>(token) }
            .doOnSuccess { logger.info("Successful login for ${body.username}") }
            .doOnError { t -> logger.error(t.message) }
            .onErrorResume { t ->
                Mono.just<ResponseEntity<Any>>(
                    when (t) {
                        is UsernameNotFoundException -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body<Any>(t.message)
                        is BadCredentialsException -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body<Any>(t.message)
                        else -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body<Any>("An unknown error occurred!")
                    }
                )
            }
    }
}