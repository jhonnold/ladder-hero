package me.honnold.ladderhero.service

import me.honnold.ladderhero.model.db.FileUpload
import me.honnold.ladderhero.repository.FileUploadRepository
import me.honnold.ladderhero.service.aws.S3ClientService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.nio.file.Path

@Service
class ReplayService(
    private val s3ClientService: S3ClientService,
    private val fileUploadRepository: FileUploadRepository
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ReplayService::class.java)
    }

    fun processNewReplay(fileUpload: FileUpload): Mono<Path> {
        fileUpload.status = "PROCESSING"

        return fileUploadRepository.save(fileUpload)
            .flatMap { this.s3ClientService.download(it.key) }

    }
}