package me.honnold.ladderhero.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import java.util.*

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
    var playedAt: Date,

    var slug: String? = null
)