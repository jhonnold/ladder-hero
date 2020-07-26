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
@Entity(name = "replays")
open class Replay {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    open var id: UUID? = null

    @OneToOne
    @JoinColumn(name = "file_upload_id")
    open var fileUpload: FileUpload? = null

    @Column(name = "file_upload_id", insertable = false, updatable = false)
    open var fileUploadId: UUID? = null

    @Column(name = "map_nm")
    open lateinit var mapName: String

    @Column(name = "dur_s")
    open var duration: Long = 0

    @Column(name = "played_at")
    open lateinit var playedAt: Date

    @Column(name = "slug", unique = true)
    open lateinit var slug: String

    @OneToMany(mappedBy = "replay", targetEntity = Summary::class, fetch = FetchType.EAGER)
    open var summaries: Set<Summary> = HashSet()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val that = other as Replay
        return this.id == that.id
    }

    override fun hashCode(): Int = if (id != null) id.hashCode() else 0

    override fun toString(): String = "Replay(id=$id)"
}