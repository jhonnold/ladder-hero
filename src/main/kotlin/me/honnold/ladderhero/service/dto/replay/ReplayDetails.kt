package me.honnold.ladderhero.service.dto.replay

import org.json.simple.JSONObject
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
        val teamId: Long = 0,
        val didWin: Boolean = false,
        val profileId: Long = 0,
        val totalLostMinerals: Long = 0,
        val totalLostVespene: Long = 0,
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
            val lostVespene: Long = 0,
            val unspentMinerals: Long = 0,
            val unspentVespene: Long = 0,
            val collectionRateMinerals: Long = 0,
            val collectionRateVespene: Long = 0,
            val activeWorkers: Long = 0,
            val armyValueMinerals: Long = 0,
            val armyValueVespene: Long = 0,
            val activeUnits: JSONObject? = null
        )
    }
}
