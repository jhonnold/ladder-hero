package me.honnold.ladderhero.service

import me.honnold.ladderhero.model.db.FileUpload
import me.honnold.ladderhero.repository.FileUploadRepository
import me.honnold.ladderhero.service.aws.S3ClientService
import org.slf4j.LoggerFactory
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.util.*

@Service
class FileUploadService(private val s3ClientService: S3ClientService, private val fileUploadRepository: FileUploadRepository) {
    companion object {
        private val logger = LoggerFactory.getLogger(FileUploadService::class.java)
    }

    fun handleUpload(files: Flux<FilePart>): Flux<FileUpload> {
        return files
                .doOnNext { logger.info("New replay received: ${it.filename()}") }
                .flatMap { s3ClientService.upload(it) }
                .map { FileUpload(key = UUID.fromString(it.first), fileName = it.second) }
                .flatMap { fileUploadRepository.save(it) }
                .doOnNext { logger.info("Saved $it") }
    }
}