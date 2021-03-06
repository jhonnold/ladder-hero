package me.honnold.ladderhero.service.domain

import me.honnold.ladderhero.dao.FileUploadDAO
import me.honnold.ladderhero.dao.domain.FileUpload
import me.honnold.ladderhero.service.dto.upload.UploadResult
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class FileService(private val fileUploadDAO: FileUploadDAO) {
    fun saveUploadResult(result: UploadResult): Mono<UploadResult> {
        val fileUpload = FileUpload(key = result.fileKey, fileName = result.fileName)

        return this.fileUploadDAO.save(fileUpload).onErrorResume { Mono.empty() }.map { result }
    }
}
