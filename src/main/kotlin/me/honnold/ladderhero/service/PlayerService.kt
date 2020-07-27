package me.honnold.ladderhero.service

import me.honnold.ladderhero.domain.Player
import me.honnold.ladderhero.repository.PlayerRepository
import me.honnold.ladderhero.util.unescapeName
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
    }

    fun buildPlayers(data: ReplayService.ReplayData): Flux<PlayerData> {
        val players: List<Struct> = data.details["m_playerList"]
        logger.debug("Replay included ${players.size} player(s)")

        return Flux.fromIterable(players)
            .flatMap {
                val playerId: Long = it["m_workingSetSlotId"]
                val raceBlob: Blob = it["m_race"]
                val race = raceBlob.value

                val toon: Struct = it["m_toon"]
                val profileId: Long = toon["m_id"]
                val regionId: Long = toon["m_region"]
                val realmId: Long = toon["m_realm"]
                val nameBlob: Blob = it["m_name"]
                val name = unescapeName(nameBlob.value)

                this.playerRepository.existsByProfileId(profileId)
                    .flatMap { exists ->
                        if (exists)
                            this.playerRepository.findByProfileId(profileId)
                        else
                            this.save(profileId, regionId, realmId)
                                .onErrorResume {
                                    logger.warn("Conflict on saving player with $profileId, attempting lookup again...")
                                    this.playerRepository.findByProfileId(profileId)
                                }
                    }
                    .map { p -> PlayerData(p, playerId + 1, race, name) }
            }
    }

    fun save(profileId: Long, regionId: Long, realmId: Long): Mono<Player> {
        val player = Player(profileId = profileId, regionId = regionId, realmId = realmId)

        return this.playerRepository.save(player)
            .doFirst { logger.debug("Saving new player ($profileId)") }
            .doOnSuccess { logger.debug("Successfully saved new $it") }
            .doOnError { logger.error("Unable to save new player ($profileId) -- ${it.message}") }
    }

    data class PlayerData(val player: Player, val id: Long, val race: String, val name: String)
}