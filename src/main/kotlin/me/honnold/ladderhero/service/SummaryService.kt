package me.honnold.ladderhero.service

import me.honnold.ladderhero.model.db.Replay
import me.honnold.ladderhero.model.db.Summary
import me.honnold.ladderhero.repository.SummaryRepository
import me.honnold.sc2protocol.model.data.Struct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class SummaryService(private val summaryRepository: SummaryRepository) {
    companion object {
        private val logger = LoggerFactory.getLogger(SummaryService::class.java)
    }

    fun initializeSummary(replay: Replay, playerData: PlayerService.PlayerData): Mono<Summary> {
        val summary = Summary(
            replayId = replay.id,
            playerId = playerData.player.id,
            workingId = playerData.id,
            race = playerData.race,
            name = playerData.name
        )

        return this.summaryRepository.save(summary)
            .doOnSuccess { logger.info("Successfully initialized summary for ${playerData.player.id} on ${replay.id} as $it") }
    }

    fun populateSummary(summary: Summary, data: ReplayService.ReplayData): Mono<Summary> {
        logger.debug("Starting to populate stats for $summary")

        val firstLeaveGameEvent = data.gameEvents.find { it.name == "NNet.Game.SGameUserLeaveEvent" }
        val maxGameLoop = firstLeaveGameEvent!!.loop

        val summaryStateEvents = data.trackerEvents.filter {
            if (it.name != "NNet.Replay.Tracker.SPlayerStatsEvent")
                false
            else {
                val playerId: Long = it.data["m_playerId"]
                playerId.toInt() == summary.workingId
            }
        }

        logger.debug("Found ${summaryStateEvents.size} events for ${summary.workingId}")

        // TODO: Grab and store snapshots

        val finalSummaryStateEvent = summaryStateEvents.findLast { it.loop < maxGameLoop }!!
        val stats: Struct = finalSummaryStateEvent.data["m_stats"]

        val collectedMinerals = stats.getLong("m_scoreValueMineralsCurrent") +
                stats.getLong("m_scoreValueMineralsUsedInProgressArmy") +
                stats.getLong("m_scoreValueMineralsUsedInProgressEconomy") +
                stats.getLong("m_scoreValueMineralsUsedInProgressTechnology") +
                stats.getLong("m_scoreValueMineralsUsedCurrentArmy") +
                stats.getLong("m_scoreValueMineralsUsedCurrentEconomy") +
                stats.getLong("m_scoreValueMineralsUsedCurrentTechnology") +
                stats.getLong("m_scoreValueMineralsLostArmy") +
                stats.getLong("m_scoreValueMineralsLostEconomy") +
                stats.getLong("m_scoreValueMineralsLostTechnology")

        val collectedVespene = stats.getLong("m_scoreValueVespeneCurrent") +
                stats.getLong("m_scoreValueVespeneUsedInProgressArmy") +
                stats.getLong("m_scoreValueVespeneUsedInProgressEconomy") +
                stats.getLong("m_scoreValueVespeneUsedInProgressTechnology") +
                stats.getLong("m_scoreValueVespeneUsedCurrentArmy") +
                stats.getLong("m_scoreValueVespeneUsedCurrentEconomy") +
                stats.getLong("m_scoreValueVespeneUsedCurrentTechnology") +
                stats.getLong("m_scoreValueVespeneLostArmy") +
                stats.getLong("m_scoreValueVespeneLostEconomy") +
                stats.getLong("m_scoreValueVespeneLostTechnology")

        val lostMinerals = stats.getLong("m_scoreValueMineralsLostArmy")
        val lostVespene = stats.getLong("m_scoreValueVespeneLostArmy")

        logger.info("Found $collectedMinerals and $collectedVespene and $lostMinerals and $lostVespene")

        return Mono.empty()
    }
}

fun Struct.getLong(key: String): Long = this[key]
