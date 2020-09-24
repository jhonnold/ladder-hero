package me.honnold.ladderhero.service

import me.honnold.ladderhero.service.domain.FileService
import me.honnold.ladderhero.service.dto.upload.UploadResult
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*

@Service
class UploadService(
    private val s3ClientService: S3ClientService,
    private val fileService: FileService,
    private val replayProcessingService: ReplayProcessingService,
    private val tempDir: String
) {
    @Value("\${aws.offline}")
    private var offline = true

    fun uploadFiles(files: Flux<FilePart>): Flux<UploadResult> {
        val uploadFlux = if (offline) {
            files.flatMap { file ->
                val id = UUID.randomUUID()
                val path = Paths.get(tempDir, "$id.SC2Replay")

                DataBufferUtils.write(file.content(), path, StandardOpenOption.CREATE_NEW)
                    .then(Mono.just(UploadResult(id, file.filename())))
            }
        } else {
            files
                .flatMap { part -> this.s3ClientService.upload(part) }
                .flatMap { result -> this.fileService.saveUploadResult(result) }
        }

        return uploadFlux.doOnNext { result ->
            this.replayProcessingService.processUploadAsReplay(result)
        }
    }
}
