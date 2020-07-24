package me.honnold.ladderhero.repository

import me.honnold.ladderhero.model.db.FileUpload
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import java.util.*

interface FileUploadRepository : ReactiveCrudRepository<FileUpload, UUID>