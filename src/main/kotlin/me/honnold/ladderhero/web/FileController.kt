package me.honnold.ladderhero.web

import me.honnold.ladderhero.service.UploadService
import me.honnold.ladderhero.service.dto.upload.UploadResult
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
class FileController(private val uploadService: UploadService) {
    companion object {
        private val logger = LoggerFactory.getLogger(FileController::class.java)
    }

    @PostMapping(path = ["/upload"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun upload(@RequestPart files: Flux<FilePart>): Mono<List<UploadResult>> {
        return this.uploadService.uploadFiles(files)
            .collectList()
            .doOnSuccess { logger.info("Successfully uploaded ${it.size} files") }
    }
}