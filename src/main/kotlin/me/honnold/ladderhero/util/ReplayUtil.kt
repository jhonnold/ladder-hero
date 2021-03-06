package me.honnold.ladderhero.util

import me.honnold.s2protocol.model.data.Struct

class ReplayUtil {
    companion object {
        fun findPlayerInReplayDetails(profileId: Long, replayDetails: Struct): Pair<Int, Struct?> {
            val players: List<Struct> = replayDetails["m_playerList"]

            val idx = players.indexOfFirst { player ->
                val toon: Struct = player["m_toon"]
                val id: Long = toon["m_id"]

                profileId == id
            }

            return if (idx >= 0) Pair(idx, players[idx]) else Pair(idx, null)
        }
    }
}
