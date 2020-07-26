package me.honnold.ladderhero.service

import me.honnold.ladderhero.domain.dao.PlayerDAO
import me.honnold.ladderhero.domain.model.Player
import me.honnold.ladderhero.util.unescapeName
import me.honnold.sc2protocol.model.data.Blob
import me.honnold.sc2protocol.model.data.Struct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class PlayerService(private val playerDAO: PlayerDAO) {
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

                val player = Player()
                player.profileId = profileId
                player.regionId = regionId
                player.realmId = realmId

                this.playerDAO.findOrCreatePlayer(player)
                    .map { PlayerData(it, playerId + 1, race, name) }
            }
    }

    data class PlayerData(val player: Player, val id: Long, val race: String, val name: String)
}