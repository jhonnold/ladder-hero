package me.honnold.ladderhero.domain

import org.springframework.data.annotation.Id
import java.util.*

data class Summary(
    @Id
    var id: UUID? = null,

    var replayId: UUID?,
    var playerId: UUID?,

    var workingId: Long,
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