package me.honnold.ladderhero.service

import me.honnold.ladderhero.domain.FileUpload
import me.honnold.ladderhero.repository.FileUploadRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class FileService(private val fileUploadRepository: FileUploadRepository) {
    companion object {
        private val logger = LoggerFactory.getLogger(FileService::class.java)
    }

    fun save(uploadResult: S3ClientService.UploadResult): Mono<FileUpload> {
        val fileUpload = FileUpload(key = uploadResult.fileKey, fileName = uploadResult.fileName)

        return this.save(fileUpload)
    }

    private fun save(fileUpload: FileUpload): Mono<FileUpload> {
        return this.fileUploadRepository.save(fileUpload)
            .doFirst { logger.debug("Saving new upload record $fileUpload") }
            .doOnSuccess { result -> logger.debug("Successfully saved new $result") }
            .doOnError { error -> logger.error("Unable to save upload record $fileUpload -- ${error.message}") }
    }
}