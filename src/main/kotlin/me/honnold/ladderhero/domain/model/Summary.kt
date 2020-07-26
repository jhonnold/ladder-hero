package me.honnold.ladderhero.domain.model

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import org.hibernate.annotations.GenericGenerator
import java.util.*
import javax.persistence.*
import kotlin.collections.HashSet

@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator::class,
    property = "id"
)
@Entity(name = "summaries")
open class Summary {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    open var id: UUID? = null

    @ManyToOne
    @JoinColumn(name = "replay_id")
    open var replay: Replay? = null

    @Column(name = "replay_id", insertable = false, updatable = false)
    open var replayId: UUID? = null

    @ManyToOne
    @JoinColumn(name = "player_id")
    open var player: Player? = null

    @Column(name = "player_id", insertable = false, updatable = false)
    open var playerId: UUID? = null

    @Column(name = "working_id")
    open var workingId: Long = 0

    @Column(name = "race")
    open lateinit var race: String

    @Column(name = "name")
    open lateinit var name: String

    @Column(name = "collected_minerals")
    open var collectedMinerals: Long = 0

    @Column(name = "collected_vespene")
    open var collectedVespene: Long = 0

    @Column(name = "lost_minerals")
    open var lostMinerals: Long = 0

    @Column(name = "lost_vespene")
    open var lostVespene: Long = 0

    @Column(name = "avg_unspent_minerals")
    open var avgUnspentMinerals: Long = 0

    @Column(name = "avg_unspent_vespene")
    open var avgUnspentVespene: Long = 0

    @Column(name = "avg_collection_rate_minerals")
    open var avgCollectionRateMinerals: Long = 0

    @Column(name = "avg_collection_rate_vespene")
    open var avgCollectionRateVespene: Long = 0

    @OneToMany(mappedBy = "summary", targetEntity = SummarySnapshot::class, fetch = FetchType.EAGER)
    open var snapshots: Set<SummarySnapshot> = HashSet()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val that = other as Summary
        return this.id == that.id
    }

    override fun hashCode(): Int = if (id != null) id.hashCode() else 0

    override fun toString(): String = "Summary(id=$id)"
}