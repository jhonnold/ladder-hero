package me.honnold.ladderhero.domain

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import java.util.*

data class SummarySnapshot(
    @Id
    var id: UUID? = null,

    var summaryId: UUID?,

    var loop: Long = 0,

    var lostMinerals: Long = 0,
    var lostVespene: Long = 0,

    var unspentMinerals: Long = 0,
    var unspentVespene: Long = 0,

    var collectionRateMinerals: Long = 0,
    var collectionRateVespene: Long = 0,

    var activeWorkers: Long = 0,

    var armyValueMinerals: Long = 0,
    var armyValueVespene: Long = 0
) {
    @Transient
    var summary: Summary? = null
}