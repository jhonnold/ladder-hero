package me.honnold.ladderhero.service

import me.honnold.ladderhero.service.S3ClientService.Companion.TEMP_DIR
import me.honnold.ladderhero.service.domain.PlayerService
import me.honnold.ladderhero.service.domain.ReplayService
import me.honnold.ladderhero.service.domain.SummaryService
import me.honnold.ladderhero.service.dto.replay.ReplayData
import me.honnold.ladderhero.service.dto.upload.UploadResult
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.util.function.Tuples
import java.nio.file.Paths

@Service
class ReplayProcessingService(
    private val s3ClientService: S3ClientService,
    private val summaryService: SummaryService,
    private val replayService: ReplayService,
    private val playerService: PlayerService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ReplayService::class.java)
    }

    @Value("\${aws.offline}")
    private var offline = true

    fun processUploadAsReplay(uploadResult: UploadResult) {
        val pathMono = if (offline) {
            val path = Paths.get(TEMP_DIR, "${uploadResult.fileKey}.SC2Replay")
            Mono.just(path)
        } else {
            this.s3ClientService.download(uploadResult.fileKey)
        }

        pathMono.map { path -> ReplayData(path) }
            .flatMap { data ->
                this.replayService.buildAndSaveReplay(data).map { replay ->
                    Tuples.of(data, replay)
                }
            }
            .flatMapMany { sequenceData ->
                val data = sequenceData.t1
                val replay = sequenceData.t2

                this.playerService.buildAndSavePlayers(data).map { playerData ->
                    Tuples.of(data, replay, playerData)
                }
            }
            .flatMap { sequenceData ->
                val data = sequenceData.t1
                val replay = sequenceData.t2
                val playerData = sequenceData.t3

                this.summaryService.buildAndSaveSummary(data, replay, playerData).map { summary ->
                    Tuples.of(data, replay, playerData, summary)
                }
            }
            .flatMap { sequenceData ->
                val data = sequenceData.t1
                val summary = sequenceData.t4

                this.summaryService.populateSummary(summary, data).map { sequenceData }
            }
            .doOnComplete { logger.info("Finished processing $uploadResult") }
            .doOnError { t -> logger.error("Unable to process $uploadResult -- ${t.message}") }
            .subscribe()
    }
}
