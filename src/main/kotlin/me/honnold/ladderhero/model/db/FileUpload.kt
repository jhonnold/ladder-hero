package me.honnold.ladderhero.model.db

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import java.util.*

data class FileUpload(
    @Id
    var id: UUID? = null,

    var key: UUID,

    @Column("orig_file_nm")
    var fileName: String,

    var status: String = "WAITING"
)