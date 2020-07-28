package me.honnold.ladderhero.controller

import me.honnold.ladderhero.domain.FileUpload
import me.honnold.ladderhero.domain.Replay
import me.honnold.ladderhero.service.FileService
import me.honnold.ladderhero.service.ProcessingService
import me.honnold.ladderhero.service.ReplayService
import me.honnold.ladderhero.service.S3ClientService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@RestController
@RequestMapping("/api/v1/replays")
class ReplayController(
    private val s3ClientService: S3ClientService,
    private val fileService: FileService,
    private val replayService: ReplayService,
    private val processingService: ProcessingService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ReplayController::class.java)
    }

    @PostMapping(path = ["/upload"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun upload(@RequestPart files: Flux<FilePart>): Mono<List<FileUpload>> {
        return files
            .flatMap { part -> this.s3ClientService.upload(part) }
            .flatMap { result -> this.fileService.save(result) }
            .onErrorResume { Mono.empty() }
            .doOnNext { upload -> this.processingService.processNewUpload(upload).subscribe() }
            .collectList()
            .doOnSuccess { uploads -> logger.info("Successfully uploaded ${uploads.size} files.") }
    }

    @GetMapping
    fun getReplays(
        @RequestParam(defaultValue = "25") size: Int,
        @RequestParam(defaultValue = "1") page: Int
    ): Flux<Replay> {
        return this.replayService.getReplays(PageRequest.of(page - 1, size))
    }

    @GetMapping("/{lookup}")
    fun getReplay(@PathVariable lookup: String): Mono<Replay> {
        return Mono.just(lookup)
            .flatMap {
                val id = UUID.fromString(lookup)

                this.replayService.getReplay(id)
            }
            .onErrorResume {
                logger.debug("$lookup is not a UUID, looking up as slug instead")
                this.replayService.getReplay(lookup)
            }
    }
}