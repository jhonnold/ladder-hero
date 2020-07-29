package me.honnold.ladderhero.dao.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("summary_snapshot")
data class SummarySnapshot(
    @Id
    @Column("id")
    var id: UUID? = null,

    @Column("summary_id")
    var summaryId: UUID?,

    @Column("loop")
    var loop: Long = 0,

    @Column("lost_minerals")
    var lostMinerals: Long = 0,

    @Column("lost_vespene")
    var lostVespene: Long = 0,

    @Column("unspent_minerals")
    var unspentMinerals: Long = 0,

    @Column("unspent_vespene")
    var unspentVespene: Long = 0,

    @Column("collection_rate_minerals")
    var collectionRateMinerals: Long = 0,

    @Column("collection_rate_vespene")
    var collectionRateVespene: Long = 0,

    @Column("active_workers")
    var activeWorkers: Long = 0,

    @Column("army_value_minerals")
    var armyValueMinerals: Long = 0,

    @Column("army_value_vespene")
    var armyValueVespene: Long = 0
)