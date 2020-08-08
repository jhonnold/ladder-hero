package me.honnold.ladderhero.util

import me.honnold.s2protocol.model.data.Struct

class ReplayUtil {
    companion object {
        fun findPlayerInReplayDetails(profileId: Long, replayDetails: Struct): Struct? {
            val players: List<Struct> = replayDetails["m_playerList"]

            return players.find { player ->
                val toon: Struct = player["m_toon"]
                val id: Long = toon["m_id"]

                profileId == id
            }
        }
    }
}