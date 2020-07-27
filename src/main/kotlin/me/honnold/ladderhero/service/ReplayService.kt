package me.honnold.ladderhero.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.slugify.Slugify
import me.honnold.ladderhero.domain.FileUpload
import me.honnold.ladderhero.domain.Replay
import me.honnold.ladderhero.repository.FileUploadRepository
import me.honnold.ladderhero.repository.ReplayRepository
import me.honnold.ladderhero.util.gameDuration
import me.honnold.ladderhero.util.windowsTimeToDate
import me.honnold.mpq.Archive
import me.honnold.sc2protocol.Protocol
import me.honnold.sc2protocol.model.data.Blob
import me.honnold.sc2protocol.model.data.Struct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.nio.file.Files
import java.nio.file.Path

@Service
class ReplayService(
    private val s3ClientService: S3ClientService,
    private val fileUploadRepository: FileUploadRepository,
    private val playerService: PlayerService,
    private val replayRepository: ReplayRepository,
    private val summaryService: SummaryService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ReplayService::class.java)
        private val defaultProtocol = Protocol(Protocol.DEFAULT)
        private val slugger = Slugify()
    }

    fun processNewReplay(fileUpload: FileUpload): Mono<FileUpload> {
        var data: ReplayData? = null

        return Mono.just(1)
            .flatMap {
                fileUpload.status = "PROCESSING"; this.fileUploadRepository.save(fileUpload)
            }
            .flatMap { uploadRecord ->
                this.s3ClientService.download(uploadRecord.key)
                    .doOnSuccess { data = this.loadReplayData(it) }
            }
            .flatMap {
                this.buildReplay(fileUpload, data!!)
            }
            .flatMapMany { replay ->
                this.playerService.buildPlayers(data!!)
                    .map { Pair(replay, it) }
            }
            .flatMap {
                this.summaryService.initializeSummary(it.first, it.second)
            }
            .flatMap {
                this.summaryService.populateSummary(it, data!!)
            }
            .collectList()
            .flatMap {
                fileUpload.status = "COMPLETED"; this.fileUploadRepository.save(fileUpload)
            }
            .doOnSuccess {
                logger.info("Finished processing $fileUpload")
            }
            .doOnError {
                logger.error("Unable to process $fileUpload -- ${it.message}")
            }
            .onErrorResume {
                fileUpload.status = "FAILED"; this.fileUploadRepository.save(fileUpload)
            }
            .doFinally {
                data?.archive?.close()
                Files.delete(data!!.path)
            }

    }

    private fun buildReplay(upload: FileUpload?, data: ReplayData): Mono<Replay> {
        val mapName = data.metadata["Title"] as String
        val playedAt = windowsTimeToDate(data.details["m_timeUTC"])
        val duration = gameDuration(data.gameEvents)

        val players: List<Struct> = data.details["m_playerList"]
        val matchup = players
            .groupBy { val team: Long = it["m_teamId"]; team }
            .values.joinToString("-") {
                it.joinToString("") {
                    val raceBlob: Blob = it["m_race"]
                    raceBlob.value[0].toString()
                }
            }
        val slug = slugger.slugify("${playedAt.time} $matchup $mapName")

        logger.debug("Attempting to save new $slug")
        return this.replayRepository.save(
            Replay(
                fileUploadId = upload?.id,
                mapName = mapName,
                duration = duration,
                playedAt = playedAt,
                slug = slug
            )
        )
            .doOnSuccess { logger.info("Saved new replay ($slug)") }
            .doOnError { logger.error("Unable to save replay ($slug) -- ${it.message}") }
            .onErrorResume { logger.warn("The replay $slug has already been seen before"); Mono.empty() }
    }

    private fun loadReplayData(path: Path): ReplayData {
        val archive = Archive(path)
        val header = defaultProtocol.decodeHeader(archive.userData!!.content)
        val buildNo: Long = header["m_dataBuildNum"]
        val protocol = Protocol(buildNo.toInt())

        logger.info("Identified Protocol $buildNo for $path")

        return ReplayData(path, buildNo, archive, protocol)
    }

    class ReplayData(val path: Path, val buildNo: Long, val archive: Archive, val protocol: Protocol) {
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