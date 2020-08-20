package me.honnold.ladderhero.service.domain

import me.honnold.ladderhero.dao.PlayerDAO
import me.honnold.ladderhero.dao.domain.Player
import me.honnold.ladderhero.service.dto.replay.ReplayData
import me.honnold.s2protocol.model.data.Struct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class PlayerService(private val playerDAO: PlayerDAO) {
    companion object {
        private val logger = LoggerFactory.getLogger(PlayerService::class.java)
    }

    fun buildAndSavePlayers(data: ReplayData): Flux<Player> {
        val players: List<Struct> = data.details["m_playerList"]
        logger.debug("Replay included ${players.size} player(s)")

        return Flux.fromIterable(players).flatMap { struct ->
            val toon: Struct = struct["m_toon"]
            val profileId: Long = toon["m_id"]
            val regionId: Long = toon["m_region"]
            val realmId: Long = toon["m_realm"]

            val player = Player(null, profileId, regionId, realmId)

            this.playerDAO.save(player).onErrorResume { this.playerDAO.findByProfileId(profileId) }
        }
    }
}
