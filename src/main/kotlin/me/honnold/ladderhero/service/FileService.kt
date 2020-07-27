package me.honnold.ladderhero.service

import me.honnold.ladderhero.domain.FileUpload
import me.honnold.ladderhero.repository.FileUploadRepository
import org.slf4j.LoggerFactory
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Service
class FileService(
    private val s3ClientService: S3ClientService,
    private val fileUploadRepository: FileUploadRepository,
    private val replayService: ReplayService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(FileService::class.java)
    }

    fun handleUpload(files: Flux<FilePart>): Flux<FileUpload> {
        return files
            .flatMap({ s3ClientService.upload(it) }, 8)
            .flatMap {
                this.save(it).onErrorResume { Mono.empty() }
            }
    }

    fun save(clientRecord: Pair<String, String>): Mono<FileUpload> {
        val key = UUID.fromString(clientRecord.first)
        val fileName = clientRecord.second

        val fileUpload = FileUpload(key = key, fileName = fileName)

        return this.fileUploadRepository.save(fileUpload)
            .doFirst { logger.debug("Saving new upload record ($key, $fileName)") }
            .doOnSuccess { logger.debug("Successfully saved new $it") }
            .doOnError { logger.error("Unable to save upload record ($key, $fileName) -- ${it.message}") }
    }
}