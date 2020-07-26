package me.honnold.ladderhero.controller

import me.honnold.ladderhero.domain.model.FileUpload
import me.honnold.ladderhero.service.FileService
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/api/v1/replays")
class ReplayController(private val fileService: FileService) {
    companion object {
        private val logger = LoggerFactory.getLogger(ReplayController::class.java)
    }

    @PostMapping(path = ["/upload"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun upload(@RequestPart files: Flux<FilePart>): Flux<FileUpload> {
        return this.fileService.handleUpload(files)
    }
}