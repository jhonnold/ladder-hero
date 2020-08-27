package me.honnold.ladderhero.service.domain

import com.github.slugify.Slugify
import me.honnold.ladderhero.dao.PlayerDAO
import me.honnold.ladderhero.dao.ReplayDAO
import me.honnold.ladderhero.dao.SummaryDAO
import me.honnold.ladderhero.dao.SummarySnapshotDAO
import me.honnold.ladderhero.dao.domain.Replay
import me.honnold.ladderhero.dao.domain.SummarySnapshot
import me.honnold.ladderhero.service.dto.replay.ReplayData
import me.honnold.ladderhero.service.dto.replay.ReplaySummary
import me.honnold.ladderhero.service.dto.replay.v1.ReplayDetailsV1
import me.honnold.ladderhero.service.dto.replay.v2.ReplayDetails
import me.honnold.ladderhero.service.dto.replay.v2.ReplayPlayer
import me.honnold.ladderhero.util.gameDuration
import me.honnold.ladderhero.util.isUUID
import me.honnold.ladderhero.util.toUUID
import me.honnold.ladderhero.util.windowsTimeToDate
import me.honnold.s2protocol.model.data.Blob
import me.honnold.s2protocol.model.data.Struct
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.ZoneOffset
import kotlin.math.roundToLong

@Service
class ReplayService(
    private val replayDAO: ReplayDAO,
    private val summaryDAO: SummaryDAO,
    private val playerDAO: PlayerDAO,
    private val summarySnapshotDAO: SummarySnapshotDAO
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ReplayService::class.java)
        private val slugger = Slugify()
    }

    fun getReplays(pageRequest: PageRequest, profileId: Long?): Flux<ReplaySummary> {
        val replaySummaryRows =
            if (profileId == null) this.replayDAO.findAll(pageRequest)
            else this.replayDAO.findAllByProfileId(profileId, pageRequest)

        return replaySummaryRows
            .groupBy { row -> row.replayId }
            .flatMap { replayFlux ->
                replayFlux.collectList().map { replayRows ->
                    val replaySummary =
                        ReplaySummary(
                            replayRows[0].replayId,
                            replayRows[0].mapName,
                            replayRows[0].duration,
                            replayRows[0].playedAt,
                            replayRows[0].replaySlug
                        )

                    replaySummary.players.addAll(
                        replayRows.map { player ->
                            ReplaySummary.ReplayPlayer(
                                player.playerId,
                                player.race,
                                player.name,
                                player.profileId,
                                player.teamId,
                                player.didWin,
                                player.mmr
                            )
                        }
                    )

                    replaySummary
                }
            }
            .sort { o1, o2 -> o2.playedAt.compareTo(o1.playedAt) }
    }

    fun getReplay(lookup: String): Mono<ReplayDetails> {
        val chartConfig = mapOf(
            Pair(ReplayPlayer::gameTime, listOf { s: SummarySnapshot -> (s.loop / 20.4).roundToLong() }),
            Pair(ReplayPlayer::lostResources, listOf(SummarySnapshot::lostMinerals, SummarySnapshot::lostVespene)),
            Pair(ReplayPlayer::unspentResources, listOf(SummarySnapshot::unspentMinerals, SummarySnapshot::unspentVespene)),
            Pair(ReplayPlayer::collectionRate, listOf(SummarySnapshot::collectionRateMinerals, SummarySnapshot::collectionRateVespene)),
            Pair(ReplayPlayer::activeWorkers, listOf(SummarySnapshot::activeWorkers)),
            Pair(ReplayPlayer::armyValue, listOf(SummarySnapshot::armyValueMinerals, SummarySnapshot::armyValueVespene))
        )

        val details = ReplayDetails()

        val replayMono = if (lookup.isUUID()) replayDAO.findById(lookup.toUUID())
        else replayDAO.findBySlug(lookup)

        return replayMono
            .flatMapMany { replay ->
                details.replayId = replay.id
                details.mapName = replay.mapName
                details.duration = replay.duration
                details.playedAt = replay.playedAt
                details.slug = replay.slug

                summaryDAO.findAllByReplay(replay)
            }
            .map { summary ->
                val player = ReplayPlayer(
                    summaryId = summary.id,
                    playerId = summary.playerId,
                    race = summary.race,
                    name = summary.name,
                    teamId = summary.teamId,
                    didWin = summary.didWin,
                    mmr = summary.mmr,
                    totalLostResources = summary.lostResources(),
                    totalCollectedResources = summary.collectedResources(),
                    avgUnspentResources = summary.avgUnspentResources(),
                    avgCollectionRate = summary.avgCollectionRate()
                )

                Pair(player, summarySnapshotDAO.findAllBySummary(summary))
            }
            .collectList()
            .flatMapMany { pairs ->
                details.players = pairs.map { it.first }

                Flux.concat(pairs.map { it.second })
            }
            .sort { snap1, snap2 -> (snap1.loop - snap2.loop).toInt() }
            .groupBy { it.summaryId }
            .flatMap { group ->
                val player = details.players.find { p -> p.summaryId == group.key() }
                    ?: return@flatMap Mono.empty<ReplayPlayer>()

                val cachedGroup = group.cache()

                Flux.fromIterable(chartConfig.entries)
                    .flatMap { (setter, getters) ->
                        Flux.from(cachedGroup)
                            .map { getters.fold(0L) { t, g -> t + g.invoke(it) } }
                            .collectList()
                            .map { setter.set(player, it) }
                    }
                    .map { player }
            }
            .collectList()
            .map { details }
    }

    fun getReplayV1(lookup: String): Mono<ReplayDetailsV1> {
        val replayDetailsRows =
            if (lookup.isUUID()) this.replayDAO.findDetailsById(lookup.toUUID())
            else this.replayDAO.findDetailsBySlug(lookup)

        return replayDetailsRows.collectList().flatMap { details ->
            if (details.size == 0) return@flatMap Mono.empty<ReplayDetailsV1>()

            val replayDetails =
                ReplayDetailsV1(
                    details[0].replayId,
                    details[0].mapName,
                    details[0].duration,
                    details[0].playedAt,
                    details[0].replaySlug
                )

            replayDetails.players.addAll(
                details.groupBy { row -> row.playerId }.values.map { playerSnapshots ->
                    val playerDetails =
                        ReplayDetailsV1.ReplayPlayer(
                            playerSnapshots[0].playerId,
                            playerSnapshots[0].race,
                            playerSnapshots[0].name,
                            playerSnapshots[0].teamId,
                            playerSnapshots[0].didWin,
                            playerSnapshots[0].mmr,
                            playerSnapshots[0].profileId,
                            playerSnapshots[0].totalLostMinerals,
                            playerSnapshots[0].totalLostVespene,
                            playerSnapshots[0].collectedMinerals,
                            playerSnapshots[0].collectedVespene,
                            playerSnapshots[0].avgUnspentMinerals,
                            playerSnapshots[0].avgUnspentVespene,
                            playerSnapshots[0].avgCollectionRateMinerals,
                            playerSnapshots[0].avgCollectionRateVespene
                        )

                    playerDetails.snapshots.addAll(
                        playerSnapshots.map { snapshot ->
                            val activeUnits =
                                JSONParser().parse(snapshot.activeUnits.asString()) as JSONObject

                            ReplayDetailsV1.ReplayPlayer.PlayerSnapshot(
                                snapshot.loop,
                                snapshot.lostMinerals,
                                snapshot.lostVespene,
                                snapshot.unspentMinerals,
                                snapshot.unspentVespene,
                                snapshot.collectionRateMinerals,
                                snapshot.collectionRateVespene,
                                snapshot.activeWorkers,
                                snapshot.armyValueMinerals,
                                snapshot.armyValueVespene,
                                activeUnits
                            )
                        }
                    )

                    playerDetails
                }
            )

            Mono.just(replayDetails)
        }
    }

    fun buildAndSaveReplay(replayData: ReplayData): Mono<Replay> {
        val mapName = replayData.metadata.title
        val playedAt = windowsTimeToDate(replayData.details["m_timeUTC"])
        val duration = gameDuration(replayData.gameEvents)

        val players: List<Struct> = replayData.details["m_playerList"]
        val matchup =
            players
                .groupBy {
                    val team: Long = it["m_teamId"]
                    team
                }
                .values
                .joinToString("-") { t ->
                    t.joinToString("") { p ->
                        val raceBlob: Blob = p["m_race"]
                        raceBlob.value[0].toString()
                    }
                }
        val slug = slugger.slugify("$matchup $mapName ${playedAt.toEpochSecond(ZoneOffset.UTC)}")

        logger.debug("Attempting to save new $slug")
        val replay =
            Replay(mapName = mapName, duration = duration, playedAt = playedAt, slug = slug)

        return this.replayDAO.save(replay)
    }
}
