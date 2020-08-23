package me.honnold.ladderhero.service.dto.replay

import java.time.LocalDateTime
import java.util.*

data class ReplaySummary(
    var replayId: UUID = UUID.randomUUID(),
    var mapName: String = "",
    var duration: Long = 0,
    var playedAt: LocalDateTime = LocalDateTime.MIN,
    var slug: String = ""
) {
    val players: MutableList<ReplayPlayer> = ArrayList()

    data class ReplayPlayer(
        val playerId: UUID,
        val race: String,
        val name: String,
        val profileId: Long,
        val teamId: Long,
        var didWin: Boolean,
        var mmr: Long
    )
}
