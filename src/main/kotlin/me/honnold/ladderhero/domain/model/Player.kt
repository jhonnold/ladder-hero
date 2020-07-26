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
@Entity(name = "players")
open class Player {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    open var id: UUID? = null

    @Column(name = "profile_id", unique = true)
    open var profileId: Long = 0

    @Column(name = "region_id")
    open var regionId: Long = 0

    @Column(name = "realm_id")
    open var realmId: Long = 0

    @OneToMany(mappedBy = "player", targetEntity = Summary::class, fetch = FetchType.EAGER)
    open var summaries: Set<Summary> = HashSet()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val that = other as Player
        return this.id == that.id
    }

    override fun hashCode(): Int = if (id != null) id.hashCode() else 0

    override fun toString(): String = "Player(id=$id)"
}