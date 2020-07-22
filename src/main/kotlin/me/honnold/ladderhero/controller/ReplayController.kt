package me.honnold.ladderhero.controller

import me.honnold.ladderhero.model.db.FileUpload
import me.honnold.ladderhero.service.FileUploadService
import me.honnold.ladderhero.service.aws.S3ClientService
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.stream.Collectors

@RestController
@RequestMapping("/api/v1/replays")
class ReplayController(private val fileUploadService: FileUploadService) {
    companion object {
        private val logger = LoggerFactory.getLogger(ReplayController::class.java)
    }

    @PostMapping(path = ["/upload"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun upload(@RequestPart files: Flux<FilePart>): Flux<FileUpload> {
        return this.fileUploadService.handleUpload(files)
    }
}