package me.honnold.ladderhero.service

import com.fasterxml.jackson.databind.ObjectMapper
import me.honnold.ladderhero.dao.FileUploadDAO
import me.honnold.ladderhero.dao.domain.FileUpload
import me.honnold.ladderhero.service.domain.ReplayService
import me.honnold.mpq.Archive
import me.honnold.sc2protocol.Protocol
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.nio.file.Files
import java.nio.file.Path

@Service
class ProcessingService(
    private val fileUploadDAO: FileUploadDAO,
    private val s3ClientService: S3ClientService,
    private val summaryService: SummaryService,
    private val replayService: ReplayService,
    private val playerService: PlayerService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ReplayService::class.java)
        private val defaultProtocol = Protocol(Protocol.DEFAULT)
    }

    fun processNewUpload(fileUpload: FileUpload): Mono<FileUpload> {
        var processingData: ReplayProcessingData? = null

        return Mono.just(1)
            .flatMap {
                fileUpload.status = "PROCESSING"; this.fileUploadDAO.save(fileUpload)
            }
            .flatMap { uploadRecord ->
                this.s3ClientService.download(uploadRecord.key)
                    .doOnSuccess { processingData = this.loadReplayData(it) }
            }
            .flatMap {
                this.replayService.initializeReplay(fileUpload, processingData!!)
            }
            .flatMapMany { replay ->
                this.playerService.buildPlayers(processingData!!)
                    .map { Pair(replay, it) }
            }
            .flatMap {
                this.summaryService.initializeSummary(it.first, it.second)
            }
            .flatMap {
                this.summaryService.populateSummary(it, processingData!!)
            }
            .collectList()
            .flatMap {
                fileUpload.status = "COMPLETED"; this.fileUploadDAO.save(fileUpload)
            }
            .doOnSuccess {
                logger.info("Finished processing $fileUpload")
            }
            .doOnError {
                logger.error("Unable to process $fileUpload -- ${it.message}")
            }
            .onErrorResume {
                fileUpload.status = "FAILED"; this.fileUploadDAO.save(fileUpload)
            }
            .doFinally {
                processingData?.archive?.close()
                Files.delete(processingData!!.path)
            }

    }

    private fun loadReplayData(path: Path): ReplayProcessingData {
        val archive = Archive(path)
        val header = defaultProtocol.decodeHeader(archive.userData!!.content)
        val buildNo: Long = header["m_dataBuildNum"]
        val protocol = Protocol(buildNo.toInt())

        logger.info("Identified Protocol $buildNo for $path")

        return ReplayProcessingData(path, buildNo, archive, protocol)
    }

    class ReplayProcessingData(val path: Path, val buildNo: Long, val archive: Archive, val protocol: Protocol) {
        companion object {
            private const val METADATA_FILE_NAME = "replay.gamemetadata.json"
            private const val INIT_DATA_FILE_NAME = "replay.initData"
            private const val DETAILS_FILE_NAME = "replay.details"
            private const val TRACKER_EVENTS_FILE_NAME = "replay.tracker.events"
            private const val GAME_EVENTS_FILE_NAME = "replay.game.events"
        }

        val metadata: Map<*, *> =
            ObjectMapper().readValue(archive.getFileContents(METADATA_FILE_NAME).array(), Map::class.java)
        val initData = this.protocol.decodeInitData(archive.getFileContents(INIT_DATA_FILE_NAME))
        val details = this.protocol.decodeDetails(archive.getFileContents(DETAILS_FILE_NAME))
        val trackerEvents = this.protocol.decodeTrackerEvents(archive.getFileContents(TRACKER_EVENTS_FILE_NAME))
        val gameEvents = this.protocol.decodeGameEvents(archive.getFileContents(GAME_EVENTS_FILE_NAME))
    }
}