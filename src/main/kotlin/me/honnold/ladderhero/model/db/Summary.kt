package me.honnold.ladderhero.model.db

import org.springframework.data.annotation.Id
import java.util.*

data class Summary(
    @Id
    var id: UUID? = null,

    var replayId: UUID?,
    var playerId: UUID?,

    var workingId: Int,
    var race: String,
    var name: String,
    var collectedMinerals: Long = 0,
    var collectedVespene: Long = 0,
    var lostMinerals: Long = 0,
    var lostVespene: Long = 0,
    var avgUnspentMinerals: Long = 0,
    var avgUnspentVespene: Long = 0,
    var avgCollectionRateMinerals: Long = 0,
    var avgCollectionRateVespene: Long = 0
)