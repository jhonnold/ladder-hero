package me.honnold.ladderhero.domain.model

import org.hibernate.annotations.GenericGenerator
import java.util.*
import javax.persistence.*

@Entity(name = "file_uploads")
open class FileUpload {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    open var id: UUID? = null

    @Column(name = "key", nullable = false)
    open lateinit var key: UUID

    @Column(name = "orig_file_nm", nullable = false)
    open lateinit var fileName: String

    @Enumerated
    @Column(name = "status", nullable = false)
    open var status: Status = Status.WAITING

    enum class Status {
        WAITING,
        PROCESSING,
        COMPLETED,
        INVALID,
        FAILED
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val that = other as FileUpload
        return this.id == that.id
    }

    override fun hashCode(): Int = if (id != null) id.hashCode() else 0

    override fun toString(): String = "FileUpload(id=$id)"
}