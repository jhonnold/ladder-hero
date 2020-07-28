package me.honnold.ladderhero.domain

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.HashSet

data class Replay(
    @Id
    var id: UUID? = null,

    @Column("file_upload_id")
    var fileUploadId: UUID?,

    @Column("map_nm")
    var mapName: String,

    @Column("dur_s")
    var duration: Long,

    @Column("played_at")
    var playedAt: LocalDateTime,

    var slug: String? = null
) {
    @Transient
    var fileUpload: FileUpload? = null

    @Transient
    var summaries: List<Summary> = emptyList()
}