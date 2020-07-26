package me.honnold.ladderhero.domain.dao

import me.honnold.ladderhero.domain.model.Player
import me.honnold.ladderhero.domain.repository.PlayerRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler

@Service
class PlayerDAO(
    private val playerRepository: PlayerRepository,
    private val jdbcScheduler: Scheduler,
    private val transactionTemplate: TransactionTemplate
) {
    companion object {
        private val logger = LoggerFactory.getLogger(PlayerDAO::class.java)
    }

    fun findOrCreatePlayer(player: Player): Mono<Player> {
        logger.debug("Attempting to find or create $player")

        val lookupMono = this.findByProfileId(player.profileId)

        return this.existsByProfileId(player.profileId)
            .flatMap { if (it) lookupMono else this.save(player).onErrorResume { lookupMono } }
    }

    fun existsByProfileId(profileId: Long): Mono<Boolean> =
        Mono.defer { Mono.just(this.playerRepository.existsByProfileId(profileId)) }
            .doFirst { logger.debug("Checking existence for player by profileId $profileId") }
            .doOnSuccess { logger.debug(if (it) "Player exists with profileId $profileId" else "No player with profileId $profileId") }
            .doOnError { logger.error("Unable to determine player existence for profileId $profileId -- ${it.message}") }
            .subscribeOn(jdbcScheduler)

    fun findByProfileId(profileId: Long): Mono<Player> =
        Mono.defer { Mono.just(this.playerRepository.findByProfileId(profileId)) }
            .doFirst { logger.debug("Finding player by profileId $profileId") }
            .doOnSuccess { logger.debug(if (it != null) "Found $it" else "No player with profileId $profileId") }
            .doOnError { logger.error("Unable to find player for profileId $profileId -- ${it.message}") }
            .subscribeOn(jdbcScheduler)

    fun save(player: Player): Mono<Player> =
        Mono.fromCallable {
            this.transactionTemplate.execute {
                this.playerRepository.save(player); player
            }
        }
            .doFirst { logger.debug("Saving $player") }
            .doOnSuccess { logger.debug("Successfully saved $player") }
            .doOnError { logger.error("Unable to save $player -- ${it.message}") }
            .subscribeOn(jdbcScheduler)
}