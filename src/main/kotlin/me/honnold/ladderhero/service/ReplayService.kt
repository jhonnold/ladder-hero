package me.honnold.ladderhero.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.slugify.Slugify
import me.honnold.ladderhero.domain.dao.FileUploadDAO
import me.honnold.ladderhero.domain.dao.ReplayDAO
import me.honnold.ladderhero.domain.model.FileUpload
import me.honnold.ladderhero.domain.model.Player
import me.honnold.ladderhero.domain.model.Replay
import me.honnold.ladderhero.domain.model.Summary
import me.honnold.ladderhero.service.aws.S3ClientService
import me.honnold.ladderhero.util.gameDuration
import me.honnold.ladderhero.util.windowsTimeToDate
import me.honnold.mpq.Archive
import me.honnold.sc2protocol.Protocol
import me.honnold.sc2protocol.model.data.Blob
import me.honnold.sc2protocol.model.data.Struct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler
import java.nio.file.Files
import java.nio.file.Path

@Service
class ReplayService(
    private val s3ClientService: S3ClientService,
    private val fileUploadDAO: FileUploadDAO,
    private val playerService: PlayerService,
    private val replayDAO: ReplayDAO,
    private val summaryService: SummaryService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ReplayService::class.java)
        private val defaultProtocol = Protocol(Protocol.DEFAULT)
        private val slugger = Slugify()
    }

    fun processNewReplay(fileUpload: FileUpload): Mono<Replay> {
        var data: ReplayData? = null
        val state = ProcessingState()

        fileUpload.status = FileUpload.Status.PROCESSING

        return fileUploadDAO.save(fileUpload)
            .flatMap { this.s3ClientService.download(it.key) }
            .map { data = this.loadReplayData(it) }
            .flatMap { this.buildReplay(fileUpload, data!!) }
            .doOnNext { state.replay = it }
            .flatMapMany { this.playerService.buildPlayers(data!!) }
            .doOnNext { state.players.add(it.player) }
            .flatMap { this.summaryService.initializeSummary(state.replay!!, it) }
            .doOnNext { state.summaries.add(it) }
            .flatMap { this.summaryService.populateSummary(it, data!!) }
            .collectList()
            .flatMap {
                fileUpload.status = FileUpload.Status.COMPLETED

                this.fileUploadDAO.save(fileUpload)
            }
            .flatMap {
                val replay = state.replay
                if (replay != null)
                    Mono.just(replay)
                else
                    Mono.empty()
            }
            .doOnSuccess { logger.info("Finished processing $fileUpload, saved as ${state.replay}") }
            .doFinally {
                data?.archive?.close()
                Files.delete(data!!.path)
            }
    }

    private fun buildReplay(upload: FileUpload, data: ReplayData): Mono<Replay> {
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

        val replay = Replay()
        replay.fileUpload = upload
        replay.mapName = mapName
        replay.duration = duration
        replay.playedAt = playedAt
        replay.slug = slug

        return this.replayDAO.save(replay)
            .onErrorResume { logger.warn("The replay $slug has already been seen before"); Mono.empty() }
    }

    private fun loadReplayData(path: Path): ReplayData {
        val archive = Archive(path)
        val header = defaultProtocol.decodeHeader(archive.userData!!.content)
        val buildNo: Long = header["m_dataBuildNum"]
        val protocol = Protocol(buildNo.toInt())

        logger.info("Identified Protocol $buildNo for $path")

        return ReplayData(path, buildNo.toInt(), archive, protocol)
    }

    class ProcessingState {
        var replay: Replay? = null
        var players: MutableList<Player> = ArrayList()
        var summaries: MutableList<Summary> = ArrayList()
    }

    class ReplayData(val path: Path, val buildNo: Int, val archive: Archive, val protocol: Protocol) {
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