package me.honnold.ladderhero.service

import me.honnold.ladderhero.service.domain.FileService
import me.honnold.ladderhero.service.dto.upload.UploadResult
import org.slf4j.LoggerFactory
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class UploadService(
    private val s3ClientService: S3ClientService,
    private val fileService: FileService,
    private val replayProcessingService: ReplayProcessingService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UploadService::class.java)
    }

    fun uploadFiles(files: Flux<FilePart>): Flux<UploadResult> {
        return files
            .flatMap { part -> this.s3ClientService.upload(part) }
            .flatMap { result -> this.fileService.saveUploadResult(result) }
            .doOnNext { result -> this.replayProcessingService.processUploadAsReplay(result).subscribe() }
    }
}