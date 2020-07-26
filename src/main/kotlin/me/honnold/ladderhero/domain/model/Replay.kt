package me.honnold.ladderhero.domain.model

import org.hibernate.annotations.GenericGenerator
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity(name = "replays")
open class Replay {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    open var id: UUID? = null

    @Column(name = "file_upload_id")
    open var fileUploadId: UUID? = null

    @Column(name = "map_nm")
    open lateinit var mapName: String

    @Column(name = "dur_s")
    open var duration: Long = 0

    @Column(name = "played_at")
    open lateinit var playedAt: Date

    @Column(name = "slug", unique = true)
    open lateinit var slug: String

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val that = other as Replay
        return this.id == that.id
    }

    override fun hashCode(): Int = if (id != null) id.hashCode() else 0

    override fun toString(): String = "Replay(id=$id)"
}