package me.honnold.ladderhero.service

import me.honnold.ladderhero.domain.Replay
import me.honnold.ladderhero.domain.Summary
import me.honnold.ladderhero.domain.SummarySnapshot
import me.honnold.ladderhero.repository.PlayerRepository
import me.honnold.ladderhero.repository.SummaryRepository
import me.honnold.ladderhero.repository.SummarySnapshotRepository
import me.honnold.sc2protocol.model.data.Struct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.function.Tuples
import kotlin.math.max

@Service
class SummaryService(
    private val summaryRepository: SummaryRepository,
    private val summarySnapshotService: SummarySnapshotService,
    private val summarySnapshotRepository: SummarySnapshotRepository,
    private val playerRepository: PlayerRepository
) {
    companion object {
        private val logger = LoggerFactory.getLogger(SummaryService::class.java)
    }

    fun attachSummarySnapshots(summary: Summary): Mono<Summary> {
        return this.summarySnapshotRepository.findAllBySummaryId(summary.id!!)
            .collectList()
            .map { summary.snapshots = it; summary }
    }

    fun attachPlayer(summary: Summary): Mono<Summary> {
        val id = summary.playerId ?: return Mono.empty()

        return this.playerRepository.findById(id)
            .map { summary.player = it; summary }
    }

    fun initializeSummary(replay: Replay, playerData: PlayerService.PlayerData): Mono<Summary> {
        val summary = Summary(
            replayId = replay.id,
            playerId = playerData.player.id,
            workingId = playerData.id,
            race = playerData.race,
            name = playerData.name
        )

        return this.save(summary)
            .doOnSuccess { logger.info("Successfully initialized summary for ${playerData.player.id} on ${replay.id} as $it") }
    }

    fun populateSummary(summary: Summary, data: ProcessingService.ReplayProcessingData): Mono<Summary> {
        logger.debug("Starting to populate stats for $summary")

        val firstLeaveGameEvent = data.gameEvents.find { it.name == "NNet.Game.SGameUserLeaveEvent" }
            ?: return Mono.empty()

        val maxGameLoop = firstLeaveGameEvent.loop

        val summaryStateEvents = data.trackerEvents.filter {
            if (it.name != "NNet.Replay.Tracker.SPlayerStatsEvent" || it.loop > maxGameLoop)
                false
            else {
                val playerId: Long = it.data["m_playerId"]
                playerId == summary.workingId
            }
        }

        logger.debug("Found ${summaryStateEvents.size} stat events for ${summary.name} (${summary.workingId}) on ${summary.id}")

        val snapshots = Flux.fromIterable(summaryStateEvents)
            .map {
                val stats: Struct = it.data["m_stats"]

                val lostMinerals = stats.getLong("m_scoreValueMineralsLostArmy") +
                        stats.getLong("m_scoreValueMineralsLostEconomy") +
                        stats.getLong("m_scoreValueMineralsLostTechnology")
                val lostVespene = stats.getLong("m_scoreValueVespeneLostArmy") +
                        stats.getLong("m_scoreValueVespeneLostEconomy") +
                        stats.getLong("m_scoreValueVespeneLostTechnology")

                val unspentMinerals = stats.getLong("m_scoreValueMineralsCurrent")
                val unspentVespene = stats.getLong("m_scoreValueVespeneCurrent")

                val collectionRateMinerals = stats.getLong("m_scoreValueMineralsCollectionRate")
                val collectionRateVespene = stats.getLong("m_scoreValueVespeneCollectionRate")

                val activeWorkers = stats.getLong("m_scoreValueWorkersActiveCount")

                val armyValueMinerals = stats.getLong("m_scoreValueMineralsUsedCurrentArmy")
                val armyValueVespene = stats.getLong("m_scoreValueVespeneUsedCurrentArmy")

                SummarySnapshot(
                    null,
                    summary.id,
                    it.loop,
                    lostMinerals,
                    lostVespene,
                    unspentMinerals,
                    unspentVespene,
                    collectionRateMinerals,
                    collectionRateVespene,
                    activeWorkers,
                    armyValueMinerals,
                    armyValueVespene
                )
            }
            .collectList()
            .flatMap { this.summarySnapshotService.saveAll(it) }

        return snapshots.map {
            it.fold(Tuples.of(0L, 0L, 0L, 0L, 0L)) { acc, snapshot ->
                acc.mapT1 { it + snapshot.unspentMinerals }
                    .mapT2 { it + snapshot.unspentVespene }
                    .mapT3 { it + snapshot.collectionRateMinerals }
                    .mapT4 { it + snapshot.collectionRateVespene }
                    .mapT5 { it + 1 }
            }
        }
            .flatMap { acc ->
                val finalSummaryStateEvent = summaryStateEvents.findLast { e -> e.loop <= maxGameLoop }
                    ?: return@flatMap Mono.empty<Summary>()

                val stats: Struct = finalSummaryStateEvent.data["m_stats"]

                val lostMinerals = stats.getLong("m_scoreValueMineralsLostArmy") +
                        stats.getLong("m_scoreValueMineralsLostEconomy") +
                        stats.getLong("m_scoreValueMineralsLostTechnology")
                val lostVespene = stats.getLong("m_scoreValueVespeneLostArmy") +
                        stats.getLong("m_scoreValueVespeneLostEconomy") +
                        stats.getLong("m_scoreValueVespeneLostTechnology")

                val collectedMinerals = stats.getLong("m_scoreValueMineralsCurrent") +
                        stats.getLong("m_scoreValueMineralsUsedInProgressArmy") +
                        stats.getLong("m_scoreValueMineralsUsedInProgressEconomy") +
                        stats.getLong("m_scoreValueMineralsUsedInProgressTechnology") +
                        stats.getLong("m_scoreValueMineralsUsedCurrentArmy") +
                        stats.getLong("m_scoreValueMineralsUsedCurrentEconomy") +
                        stats.getLong("m_scoreValueMineralsUsedCurrentTechnology") +
                        lostMinerals

                val collectedVespene = stats.getLong("m_scoreValueVespeneCurrent") +
                        stats.getLong("m_scoreValueVespeneUsedInProgressArmy") +
                        stats.getLong("m_scoreValueVespeneUsedInProgressEconomy") +
                        stats.getLong("m_scoreValueVespeneUsedInProgressTechnology") +
                        stats.getLong("m_scoreValueVespeneUsedCurrentArmy") +
                        stats.getLong("m_scoreValueVespeneUsedCurrentEconomy") +
                        stats.getLong("m_scoreValueVespeneUsedCurrentTechnology") +
                        lostVespene

                summary.collectedMinerals = collectedMinerals
                summary.collectedVespene = collectedVespene
                summary.lostMinerals = lostMinerals
                summary.lostVespene = lostVespene
                summary.avgUnspentMinerals = acc.t1 / max(1, acc.t5)
                summary.avgUnspentVespene = acc.t2 / max(1, acc.t5)
                summary.avgCollectionRateMinerals = acc.t3 / max(1, acc.t5)
                summary.avgCollectionRateVespene = acc.t4 / max(1, acc.t5)

                this.save(summary)
                    .doOnSuccess { logger.info("Populated $summary") }
            }
    }

    fun save(summary: Summary): Mono<Summary> {
        return this.summaryRepository.save(summary)
            .doFirst { logger.debug("Starting to save $summary") }
            .doOnSuccess { logger.debug("Successfully saved $summary") }
            .doOnError { logger.error("Unable to save $summary -- ${it.message}") }
    }
}

fun Struct.getLong(key: String): Long = this[key]
