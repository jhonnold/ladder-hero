package me.honnold.ladderhero.service

import me.honnold.ladderhero.dao.FileUploadDAO
import me.honnold.ladderhero.dao.domain.FileUpload
import me.honnold.ladderhero.service.dto.upload.UploadResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.*

@Service
class FileService(private val fileUploadDAO: FileUploadDAO) {
    companion object {
        private val logger = LoggerFactory.getLogger(FileService::class.java)
    }

    fun saveUploadResult(result: UploadResult): Mono<FileUpload> {
        val fileUpload = FileUpload(key = result.fileKey, fileName = result.fileName)

        return this.fileUploadDAO.save(fileUpload)
    }
}