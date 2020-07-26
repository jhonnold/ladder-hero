package me.honnold.ladderhero.controller

import me.honnold.ladderhero.domain.dao.ReplayDAO
import me.honnold.ladderhero.domain.model.FileUpload
import me.honnold.ladderhero.domain.model.Replay
import me.honnold.ladderhero.service.FileService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/api/v1/replays")
open class ReplayController(private val fileService: FileService, private val replayDAO: ReplayDAO) {
    companion object {
        private val logger = LoggerFactory.getLogger(ReplayController::class.java)
    }

    @PostMapping(path = ["/upload"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun upload(@RequestPart files: Flux<FilePart>): Flux<FileUpload> {
        return this.fileService.handleUpload(files)
    }

    @GetMapping
    open fun getReplays(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "25") size: Int
    ): Flux<Replay> {
        return this.replayDAO.findAll(PageRequest.of(page, size, Sort.by("playedAt").descending()))
    }
}