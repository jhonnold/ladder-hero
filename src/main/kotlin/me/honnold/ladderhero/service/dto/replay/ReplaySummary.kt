package me.honnold.ladderhero.service.dto.replay

import java.time.LocalDateTime
import java.util.*

data class ReplaySummary(
    var replayId: UUID? = null,
    var mapName: String? = null,
    var duration: Long? = null,
    var playedAt: LocalDateTime? = null,
    var slug: String? = null
) {
    val players: MutableList<ReplayPlayer> = ArrayList()

    data class ReplayPlayer(
        val playerId: UUID,
        val race: String,
        val name: String,
        val profileId: Long
    )
}