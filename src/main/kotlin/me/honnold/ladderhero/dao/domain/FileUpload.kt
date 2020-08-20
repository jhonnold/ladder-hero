package me.honnold.ladderhero.dao.domain

import java.util.*
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("file_uploads")
data class FileUpload(
    @Id
    @Column("id")
    var id: UUID? = null,
    @Column("key")
    var key: UUID,
    @Column("orig_file_nm")
    var fileName: String,
    @Column("status")
    var status: String = "WAITING")
