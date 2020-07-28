package me.honnold.ladderhero.web

import me.honnold.ladderhero.dao.domain.FileUpload
import me.honnold.ladderhero.service.FileService
import me.honnold.ladderhero.service.ProcessingService
import me.honnold.ladderhero.service.S3ClientService
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/files")
class FileController(
    private val s3ClientService: S3ClientService,
    private val fileService: FileService,
    private val processingService: ProcessingService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(FileController::class.java)
    }

    @PostMapping(path = ["/upload"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun upload(@RequestPart files: Flux<FilePart>): Mono<List<FileUpload>> {
        return files
            .flatMap { part -> this.s3ClientService.upload(part) }
            .flatMap { result -> this.fileService.saveUploadResult(result) }
            .onErrorResume { logger.error("An issue occurred during upload! ${it.message}"); Mono.empty() }
            .doOnNext { upload -> this.processingService.processNewUpload(upload).subscribe() }
            .collectList()
            .doOnSuccess { uploads -> logger.info("Successfully uploaded ${uploads.size} files.") }
    }
}