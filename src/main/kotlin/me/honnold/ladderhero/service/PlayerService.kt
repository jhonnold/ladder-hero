package me.honnold.ladderhero.service

import me.honnold.ladderhero.model.db.Player
import me.honnold.ladderhero.repository.PlayerRepository
import me.honnold.mpq.Archive
import me.honnold.sc2protocol.Protocol
import me.honnold.sc2protocol.model.data.Blob
import me.honnold.sc2protocol.model.data.Struct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class PlayerService(private val playerRepository: PlayerRepository) {
    companion object {
        private val logger = LoggerFactory.getLogger(PlayerService::class.java)

        private const val DETAILS_FILE_NAME = "replay.details"
    }

    fun processPlayers(replayDecoding: Triple<Int, Archive, Protocol>): Flux<Player> {
        val (_, archive, protocol) = replayDecoding
        val detailsContents = archive.getFileContents(DETAILS_FILE_NAME)
        val details = protocol.decodeDetails(detailsContents)

        val players: List<Struct> = details["m_playerList"]
        logger.debug("Replay included ${players.size} player(s)")

        return Flux.fromIterable(players)
            .flatMap {
                val playerId: Long = it["m_workingSetSlotId"]
                val raceBlob: Blob = it["m_race"]

                val toon: Struct = it["m_toon"]
                val profileId: Long = toon["m_id"]
                val regionId: Long = toon["m_region"]
                val realmId: Long = toon["m_realm"]
                val name: Blob = it["m_name"]

                this.findOrCreatePlayer(
                    Player(
                        null,
                        profileId.toInt(),
                        regionId.toInt(),
                        realmId.toInt(),
                        name.value
                    )
                )
            }
    }

    private fun findOrCreatePlayer(player: Player): Mono<Player> {
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