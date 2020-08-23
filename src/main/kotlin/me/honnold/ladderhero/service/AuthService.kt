package me.honnold.ladderhero.service

import me.honnold.ladderhero.exception.UsernameAlreadyTakenException
import me.honnold.ladderhero.service.domain.UserService
import me.honnold.ladderhero.service.dto.AuthedUser
import me.honnold.ladderhero.service.dto.JWTToken
import me.honnold.ladderhero.web.request.AuthRequest
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Service
class AuthService(
    private val passwordEncoder: PasswordEncoder,
    private val userService: UserService,
    private val jwtService: JWTService,
    private val blizzardService: BlizzardService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(AuthService::class.java)
    }

    fun getMe(username: String): Mono<AuthedUser> {
        val authedUser = AuthedUser(username)

        return userService.getUser(username)
            .flatMap { user ->
                authedUser.battleNetId = user.accountId
                authedUser.battletag = user.battleTag
                authedUser.linkedToBlizzard = user.battleTag != null

                if (user.accountId > 0) {
                    blizzardService.getSc2PlayerInfo(user.accountId)
                        .map { accounts ->
                            authedUser.sc2Accounts = accounts

                            authedUser
                        }
                } else {
                    Mono.just(authedUser)
                }
            }
    }

    fun registerUser(body: AuthRequest): Mono<JWTToken> {
        val encodedPassword = passwordEncoder.encode(body.password)

        return this.userService
            .createUser(body.username, encodedPassword)
            .map { user -> this.jwtService.getJWTToken(user) }
            .onErrorMap { t ->
                when (t) {
                    is DataIntegrityViolationException ->
                        UsernameAlreadyTakenException(
                            "The username ${body.username} is already in use!"
                        )
                    else -> t
                }
            }
    }

    fun login(body: AuthRequest): Mono<JWTToken> {
        return this.userService
            .getUser(body.username)
            .flatMap { user ->
                if (passwordEncoder.matches(body.password, user.encodedPassword)) Mono.just(user)
                else BadCredentialsException("Incorrect password for ${body.username}!").toMono()
            }
            .map { user -> this.jwtService.getJWTToken(user) }
            .switchIfEmpty(
                UsernameNotFoundException("User with username ${body.username} does not exist")
                    .toMono()
            )
    }
}
