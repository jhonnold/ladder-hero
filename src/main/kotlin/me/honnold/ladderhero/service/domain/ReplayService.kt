package me.honnold.ladderhero.service.domain

import com.github.slugify.Slugify
import me.honnold.ladderhero.dao.domain.FileUpload
import me.honnold.ladderhero.dao.domain.Replay
import me.honnold.ladderhero.repository.ReplayRepository
import me.honnold.ladderhero.repository.SummaryRepository
import me.honnold.ladderhero.service.ReplayProcessingService
import me.honnold.ladderhero.service.SummaryService
import me.honnold.ladderhero.service.dto.replay.ReplayData
import me.honnold.ladderhero.util.gameDuration
import me.honnold.ladderhero.util.windowsTimeToDate
import me.honnold.sc2protocol.model.data.Blob
import me.honnold.sc2protocol.model.data.Struct
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.ZoneOffset
import java.util.*

@Service
class ReplayService(
    private val replayRepository: ReplayRepository,
    private val summaryRepository: SummaryRepository,
    private val summaryService: SummaryService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ReplayService::class.java)
        private val slugger = Slugify()
    }

    // TODO: Solve the N+1 problems that lie below this...
    fun getReplays(page: Pageable): Flux<Replay> {
        return this.replayRepository.findPage(page.pageSize, page.offset)
            .flatMap { replay ->
                this.summaryRepository.getSummariesForReplayId(replay.id!!)
                    .flatMap { this.summaryService.attachPlayer(it) }
                    .collectList()
                    .map { replay.summaries = it; replay }
            }
    }

    fun getReplay(id: UUID): Mono<Replay> {
        return this.replayRepository.findById(id)
            .flatMap { replay ->
                this.summaryRepository.getSummariesForReplayId(replay.id!!)
                    .flatMap { this.summaryService.attachPlayer(it) }
                    .flatMap { this.summaryService.attachSummarySnapshots(it) }
                    .collectList()
                    .map { replay.summaries = it; replay }
            }
    }

    fun getReplay(slug: String): Mono<Replay> {
        return this.replayRepository.findBySlug(slug)
            .flatMap { replay ->
                this.summaryRepository.getSummariesForReplayId(replay.id!!)
                    .flatMap { this.summaryService.attachPlayer(it) }
                    .flatMap { this.summaryService.attachSummarySnapshots(it) }
                    .collectList()
                    .map { replay.summaries = it; replay }
            }
    }

    fun buildAndSaveReplay(replayData: ReplayData): Mono<Replay> {
        val mapName = replayData.metadata["Title"] as String
        val playedAt = windowsTimeToDate(replayData.details["m_timeUTC"])
        val duration = gameDuration(replayData.gameEvents)

        val players: List<Struct> = replayData.details["m_playerList"]
        val matchup = players
            .groupBy { val team: Long = it["m_teamId"]; team }
            .values.joinToString("-") {
                it.joinToString("") {
                    val raceBlob: Blob = it["m_race"]
                    raceBlob.value[0].toString()
                }
            }
        val slug = slugger.slugify("$matchup $mapName ${playedAt.toEpochSecond(ZoneOffset.UTC)}")

        logger.debug("Attempting to save new $slug")
        val replay = Replay(
            mapName = mapName,
            duration = duration,
            playedAt = playedAt,
            slug = slug
        )

        return this.replayRepository.save(replay)
            .doOnSuccess { logger.info("Saved new replay ($slug)") }
            .doOnError { logger.error("Unable to save replay ($slug) -- ${it.message}") }
            .onErrorResume { logger.warn("The replay $slug has already been seen before"); Mono.empty() }
    }
}