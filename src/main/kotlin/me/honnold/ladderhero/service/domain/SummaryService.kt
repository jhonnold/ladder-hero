package me.honnold.ladderhero.service.domain

import io.r2dbc.postgresql.codec.Json
import me.honnold.ladderhero.dao.SummaryDAO
import me.honnold.ladderhero.dao.SummarySnapshotDAO
import me.honnold.ladderhero.dao.domain.Player
import me.honnold.ladderhero.dao.domain.Replay
import me.honnold.ladderhero.dao.domain.Summary
import me.honnold.ladderhero.dao.domain.SummarySnapshot
import me.honnold.ladderhero.exception.IllegalReplayException
import me.honnold.ladderhero.service.dto.replay.ReplayData
import me.honnold.ladderhero.util.ReplayUtil
import me.honnold.ladderhero.util.getLong
import me.honnold.ladderhero.util.unescapeName
import me.honnold.s2protocol.model.data.Blob
import me.honnold.s2protocol.model.data.Struct
import me.honnold.s2protocol.model.event.Event
import org.json.simple.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.util.function.Tuples
import kotlin.math.max

@Service
class SummaryService(
    private val summaryDAO: SummaryDAO,
    private val summarySnapshotDAO: SummarySnapshotDAO
) {
    companion object {
        private val logger = LoggerFactory.getLogger(SummaryService::class.java)
    }

    fun buildAndSaveSummary(data: ReplayData, replay: Replay, player: Player): Mono<Summary> {
        val playerSummary = ReplayUtil.findPlayerInReplayDetails(player.profileId, data.details)
        if (playerSummary == null) {
            logger.warn(
                "Unable to locate ${player.profileId} in ${replay.slug}! No summary will be generated"
            )
            return Mono.empty()
        }

        var workingId: Long = playerSummary["m_workingSetSlotId"]
        workingId++ // Increment this value since it's 1 idx everywhere else

        val teamId: Long = playerSummary["m_teamId"]

        val raceBlob: Blob = playerSummary["m_race"]
        val race = raceBlob.value

        val nameBlob: Blob = playerSummary["m_name"]
        val name = unescapeName(nameBlob.value)

        val jsonPlayer = data.metadata.players.find { p -> p.playerId == workingId }
        val didWin = jsonPlayer?.result == "Win"
        val mmr = jsonPlayer?.mmr ?: 0

        val summary = Summary(null, replay.id, player.id, workingId, teamId, race, name, didWin, mmr)
        return this.summaryDAO.save(summary)
    }

    fun populateSummary(summary: Summary, data: ReplayData): Mono<Summary> {
        logger.debug("Starting to populate stats for $summary")

        val firstLeaveGameEvent =
            data.gameEvents.find { it.name == "NNet.Game.SGameUserLeaveEvent" }
                ?: return Mono.error(IllegalReplayException("No user leave events found!"))

        val summaryStateEvents =
            data.trackerEvents.filter { event ->
                if (event.name != "NNet.Replay.Tracker.SPlayerStatsEvent" ||
                    event.loop > firstLeaveGameEvent.loop
                )
                    false
                else {
                    val playerId: Long = event.data["m_playerId"]
                    playerId == summary.workingId
                }
            }

        val unitEvents =
            data.trackerEvents.filter { event ->
                if (event.loop > firstLeaveGameEvent.loop) return@filter false

                if (event.name == "NNet.Replay.Tracker.SUnitDiedEvent") return@filter true

                if (event.name == "NNet.Replay.Tracker.SUnitBornEvent" ||
                    event.name == "NNet.Replay.Tracker.SUnitInitEvent"
                ) {
                    val playerId: Long = event.data["m_controlPlayerId"]

                    playerId == summary.workingId
                } else {
                    false
                }
            }

        logger.debug(
            "Found ${summaryStateEvents.size} stat events for ${summary.name} (${summary.workingId}) on ${summary.id}"
        )

        val unitMap = mutableMapOf<Long, String>()
        val unitEventIterator = unitEvents.listIterator()
        var unitEvent: Event

        val snapshots =
            summaryStateEvents
                .map { summaryStateEvent ->
                    while (unitEventIterator.hasNext()) {
                        unitEvent = unitEventIterator.next()
                        if (unitEvent.loop > summaryStateEvent.loop) {
                            unitEventIterator.previous()
                            break
                        }

                        if (unitEvent.name == "NNet.Replay.Tracker.SUnitBornEvent" ||
                            unitEvent.name == "NNet.Replay.Tracker.SUnitInitEvent"
                        ) {
                            val unitTagIndex: Long = unitEvent.data["m_unitTagIndex"]
                            val unitTagRecycle: Long = unitEvent.data["m_unitTagRecycle"]
                            val unitTypeName: Blob = unitEvent.data["m_unitTypeName"]

                            val unitId = (unitTagIndex shl 18) + unitTagRecycle
                            unitMap[unitId] = unitTypeName.value
                        } else {
                            val unitTagIndex: Long = unitEvent.data["m_unitTagIndex"]
                            val unitTagRecycle: Long = unitEvent.data["m_unitTagRecycle"]
                            val unitId = (unitTagIndex shl 18) + unitTagRecycle
                            unitMap.remove(unitId)
                        }
                    }

                    val activeUnits = unitMap.values.groupBy { it }.mapValues { it.value.size }

                    val stats: Struct = summaryStateEvent.data["m_stats"]

                    val lostMinerals =
                        stats.getLong("m_scoreValueMineralsLostArmy") +
                            stats.getLong("m_scoreValueMineralsLostEconomy") +
                            stats.getLong("m_scoreValueMineralsLostTechnology")
                    val lostVespene =
                        stats.getLong("m_scoreValueVespeneLostArmy") +
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
                        summaryStateEvent.loop,
                        lostMinerals,
                        lostVespene,
                        unspentMinerals,
                        unspentVespene,
                        collectionRateMinerals,
                        collectionRateVespene,
                        activeWorkers,
                        armyValueMinerals,
                        armyValueVespene,
                        Json.of(JSONObject.toJSONString(activeUnits))
                    )
                }
                .toMono()
                .flatMap { this.summarySnapshotDAO.saveAll(it) }

        return snapshots
            .map { snapshotsList ->
                snapshotsList.fold(Tuples.of(0L, 0L, 0L, 0L, 0L)) { acc, snapshot ->
                    snapshot.activeUnits
                        ?.asString() // This field HAS to be consumed to prevent memory leaks

                    acc
                        .mapT1 { it + snapshot.unspentMinerals }
                        .mapT2 { it + snapshot.unspentVespene }
                        .mapT3 { it + snapshot.collectionRateMinerals }
                        .mapT4 { it + snapshot.collectionRateVespene }
                        .mapT5 { it + 1 }
                }
            }
            .flatMap { acc ->
                val finalSummaryStateEvent =
                    summaryStateEvents.findLast { e -> e.loop <= firstLeaveGameEvent.loop }
                        ?: return@flatMap Mono.empty<Summary>()

                val stats: Struct = finalSummaryStateEvent.data["m_stats"]

                val lostMinerals =
                    stats.getLong("m_scoreValueMineralsLostArmy") +
                        stats.getLong("m_scoreValueMineralsLostEconomy") +
                        stats.getLong("m_scoreValueMineralsLostTechnology")
                val lostVespene =
                    stats.getLong("m_scoreValueVespeneLostArmy") +
                        stats.getLong("m_scoreValueVespeneLostEconomy") +
                        stats.getLong("m_scoreValueVespeneLostTechnology")

                val collectedMinerals =
                    stats.getLong("m_scoreValueMineralsCurrent") +
                        stats.getLong("m_scoreValueMineralsUsedInProgressArmy") +
                        stats.getLong("m_scoreValueMineralsUsedInProgressEconomy") +
                        stats.getLong("m_scoreValueMineralsUsedInProgressTechnology") +
                        stats.getLong("m_scoreValueMineralsUsedCurrentArmy") +
                        stats.getLong("m_scoreValueMineralsUsedCurrentEconomy") +
                        stats.getLong("m_scoreValueMineralsUsedCurrentTechnology") +
                        lostMinerals

                val collectedVespene =
                    stats.getLong("m_scoreValueVespeneCurrent") +
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

                this.summaryDAO.save(summary)
            }
    }
}
