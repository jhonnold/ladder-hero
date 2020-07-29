package me.honnold.ladderhero.service.dto.replay

import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

data class ReplayDetails(
    var replayId: UUID?,
    var mapName: String = "",
    var duration: Long = 0,
    var playedAt: LocalDateTime = LocalDateTime.MIN,
    var slug: String = ""
) {
    val players: MutableList<ReplayPlayer> = ArrayList()

    data class ReplayPlayer(
        val playerId: UUID?,
        val race: String = "",
        val name: String = "",
        val profileId: Long = 0,
        val collectedMinerals: Long = 0,
        val collectedVespene: Long = 0,
        val avgUnspentMinerals: Long = 0,
        val avgUnspentVespene: Long = 0,
        val avgCollectionRateMinerals: Long = 0,
        val avgCollectionRateVespene: Long = 0
    ) {
        val snapshots: MutableList<PlayerSnapshot> = ArrayList()

        data class PlayerSnapshot(
            val loop: Long = 0,
            val lostMinerals: Long = 0,
            var lostVespene: Long = 0,
            var unspentMinerals: Long = 0,
            var unspentVespene: Long = 0,
            var collectionRateMinerals: Long = 0,
            var collectionRateVespene: Long = 0,
            var activeWorkers: Long = 0,
            var armyValueMinerals: Long = 0,
            var armyValueVespene: Long = 0
        )
    }
}