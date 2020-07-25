package me.honnold.ladderhero.service

import com.fasterxml.jackson.databind.ObjectMapper
import me.honnold.ladderhero.model.db.FileUpload
import me.honnold.ladderhero.model.db.Replay
import me.honnold.ladderhero.repository.FileUploadRepository
import me.honnold.ladderhero.repository.ReplayRepository
import me.honnold.ladderhero.service.aws.S3ClientService
import me.honnold.ladderhero.util.gameDuration
import me.honnold.ladderhero.util.windowsTimeToDate
import me.honnold.mpq.Archive
import me.honnold.sc2protocol.Protocol
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.nio.file.Path

@Service
class ReplayService(
    private val s3ClientService: S3ClientService,
    private val fileUploadRepository: FileUploadRepository,
    private val playerService: PlayerService,
    private val replayRepository: ReplayRepository
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ReplayService::class.java)
        private val defaultProtocol = Protocol(Protocol.DEFAULT)
    }

    fun processNewReplay(fileUpload: FileUpload): Mono<Void> {
        var data: ReplayData? = null

        fileUpload.status = "PROCESSING"

        return fileUploadRepository.save(fileUpload)
            .flatMap { this.s3ClientService.download(it.key) }
            .map { data = this.loadReplayData(it) }
            .flatMap { this.buildReplay(fileUpload, data!!) }
            .then()
    }

    private fun buildReplay(upload: FileUpload, data: ReplayData): Mono<Replay> {
        val mapName = data.metadata["Title"] as String
        val playedAt = windowsTimeToDate(data.details["m_timeUTC"])
        val duration = gameDuration(data.gameEvents)

        val replay = Replay(
            fileUploadId = upload.id,
            mapName = mapName,
            duration = duration,
            playedAt = playedAt
        )

        logger.debug("Attempting to save new $replay")
        return this.replayRepository.save(replay)
            .doOnSuccess { logger.info("Saved new $replay") }
    }

    private fun loadReplayData(path: Path): ReplayData {
        val archive = Archive(path)
        val header = defaultProtocol.decodeHeader(archive.userData!!.content)
        val buildNo: Long = header["m_dataBuildNum"]
        val protocol = Protocol(buildNo.toInt())

        logger.info("Identified Protocol $buildNo for $path")

        return ReplayData(buildNo.toInt(), archive, protocol)
    }

    class ReplayData(val buildNo: Int, val archive: Archive, val protocol: Protocol) {
        companion object {
            private const val METADATA_FILE_NAME = "replay.gamemetadata.json"
            private const val INIT_DATA_FILE_NAME = "replay.initData"
            private const val DETAILS_FILE_NAME = "replay.details"
            private const val TRACKER_EVENTS_FILE_NAME = "replay.tracker.events"
            private const val GAME_EVENTS_FILE_NAME = "replay.game.events"
        }

        val metadata = ObjectMapper().readValue(archive.getFileContents(METADATA_FILE_NAME).array(), Map::class.java)
        val initData = this.protocol.decodeInitData(archive.getFileContents(INIT_DATA_FILE_NAME))
        val details = this.protocol.decodeDetails(archive.getFileContents(DETAILS_FILE_NAME))
        val trackerEvents = this.protocol.decodeTrackerEvents(archive.getFileContents(TRACKER_EVENTS_FILE_NAME))
        val gameEvents = this.protocol.decodeGameEvents(archive.getFileContents(GAME_EVENTS_FILE_NAME))
    }
}