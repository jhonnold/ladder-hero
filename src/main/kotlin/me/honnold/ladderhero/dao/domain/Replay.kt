package me.honnold.ladderhero.dao.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime
import java.util.*

data class Replay(
    @Id
    @Column("id")
    var id: UUID? = null,

    @Column("file_upload_id")
    var fileUploadId: UUID? = null,

    @Column("map_nm")
    var mapName: String,

    @Column("dur_s")
    var duration: Long,

    @Column("played_at")
    var playedAt: LocalDateTime,

    @Column("slug")
    var slug: String? = null
)