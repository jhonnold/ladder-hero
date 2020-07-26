package me.honnold.ladderhero.service

import me.honnold.ladderhero.domain.dao.FileUploadDAO
import me.honnold.ladderhero.domain.model.FileUpload
import me.honnold.ladderhero.service.aws.S3ClientService
import org.slf4j.LoggerFactory
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.scheduler.Scheduler
import java.util.*

@Service
class FileService(
    private val s3ClientService: S3ClientService,
    private val fileUploadDAO: FileUploadDAO,
    private val replayService: ReplayService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(FileService::class.java)
    }

    fun handleUpload(files: Flux<FilePart>): Flux<FileUpload> {
        return files
            .flatMap({ s3ClientService.upload(it) }, 8)
            .flatMap {
                val fileUpload = FileUpload()
                fileUpload.key = UUID.fromString(it.first)
                fileUpload.fileName = it.second

                this.fileUploadDAO.save(fileUpload)
            }
            .doOnNext { this.replayService.processNewReplay(it).subscribe() }
    }
}