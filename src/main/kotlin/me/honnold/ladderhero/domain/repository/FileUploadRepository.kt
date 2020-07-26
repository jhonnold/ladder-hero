package me.honnold.ladderhero.domain.repository

import me.honnold.ladderhero.domain.model.FileUpload
import org.springframework.data.repository.CrudRepository
import java.util.*

interface FileUploadRepository : CrudRepository<FileUpload, UUID>