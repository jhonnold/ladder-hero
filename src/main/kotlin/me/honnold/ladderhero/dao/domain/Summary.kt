package me.honnold.ladderhero.dao.domain

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Column
import java.util.*

data class Summary(
    @Id
    @Column("id")
    var id: UUID? = null,

    @Column("replay_id")
    var replayId: UUID?,

    @Column("player_id")
    var playerId: UUID?,

    @Column("working_id")
    var workingId: Long,

    @Column("race")
    var race: String,

    @Column("name")
    var name: String,

    @Column("collected_minerals")
    var collectedMinerals: Long = 0,

    @Column("collected_vespene")
    var collectedVespene: Long = 0,

    @Column("lost_minerals")
    var lostMinerals: Long = 0,

    @Column("lost_vespene")
    var lostVespene: Long = 0,

    @Column("avg_unspent_minerals")
    var avgUnspentMinerals: Long = 0,

    @Column("avg_unspent_vespene")
    var avgUnspentVespene: Long = 0,

    @Column("avg_collection_rate_minerals")
    var avgCollectionRateMinerals: Long = 0,

    @Column("avg_collection_rate_vespene")
    var avgCollectionRateVespene: Long = 0
)