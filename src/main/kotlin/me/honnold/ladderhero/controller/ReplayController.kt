package me.honnold.ladderhero.controller

import me.honnold.ladderhero.domain.FileUpload
import me.honnold.ladderhero.domain.Replay
import me.honnold.ladderhero.service.FileService
import me.honnold.ladderhero.service.ReplayService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/api/v1/replays")
class ReplayController(private val fileService: FileService, private val replayService: ReplayService) {
    companion object {
        private val logger = LoggerFactory.getLogger(ReplayController::class.java)
    }

    @PostMapping(path = ["/upload"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun upload(@RequestPart files: Flux<FilePart>): Flux<FileUpload> {
        return this.fileService.handleUpload(files)
    }

    @GetMapping
    fun getReplays(
        @RequestParam(defaultValue = "25") size: Int,
        @RequestParam(defaultValue = "1") page: Int
    ): Flux<Replay> {
        return this.replayService.getReplays(PageRequest.of(page - 1, size))
    }
}