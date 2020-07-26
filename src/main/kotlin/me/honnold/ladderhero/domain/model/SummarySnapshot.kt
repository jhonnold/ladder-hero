package me.honnold.ladderhero.domain.model

import org.hibernate.annotations.GenericGenerator
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity(name = "summary_snapshots")
open class SummarySnapshot {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    open var id: UUID? = null

    @Column(name = "summary_id")
    open var summaryId: UUID? = null

    @Column(name = "loop")
    open var loop: Long = 0

    @Column(name = "lost_minerals")
    open var lostMinerals: Long = 0

    @Column(name = "lost_vespene")
    open var lostVespene: Long = 0

    @Column(name = "unspent_minerals")
    open var unspentMinerals: Long = 0

    @Column(name = "unspent_vespene")
    open var unspentVespene: Long = 0

    @Column(name = "collection_rate_minerals")
    open var collectionRateMinerals: Long = 0

    @Column(name = "collection_rate_vespene")
    open var collectionRateVespene: Long = 0

    @Column(name = "active_workers")
    open var activeWorkers: Long = 0

    @Column(name = "army_value_minerals")
    open var armyValueMinerals: Long = 0

    @Column(name = "army_value_vespene")
    open var armyValueVespene: Long = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val that = other as SummarySnapshot
        return this.id == that.id
    }

    override fun hashCode(): Int = if (id != null) id.hashCode() else 0

    override fun toString(): String = "SummarySnapshot(id=$id)"
}