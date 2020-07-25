package me.honnold.ladderhero.dao

import me.honnold.ladderhero.model.db.Player
import me.honnold.ladderhero.repository.PlayerRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class PlayerDAO(private val playerRepository: PlayerRepository) {
    companion object {
        private val logger = LoggerFactory.getLogger(PlayerDAO::class.java)
    }

    fun findOrCreatePlayer(player: Player): Mono<Player> {
        logger.debug("Attempting to find or create $player")

        val lookupMono = this.playerRepository.findByProfileId(player.profileId)
            .doOnSuccess { logger.debug("Successfully found $it") }

        return this.playerRepository.existsByProfileId(player.profileId)
            .flatMap {
                if (it)
                    lookupMono
                else
                    this.playerRepository.save(player)
                        .doOnSuccess { logger.debug("Created new $it") }
                        .onErrorResume {
                            logger.warn("Conflict on saving $player, attempting lookup again...")

                            lookupMono
                        }
            }
    }
}