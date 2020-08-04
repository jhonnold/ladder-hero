package me.honnold.ladderhero.service.domain

import com.github.slugify.Slugify
import me.honnold.ladderhero.dao.ReplayDAO
import me.honnold.ladderhero.dao.domain.Replay
import me.honnold.ladderhero.service.dto.replay.ReplayData
import me.honnold.ladderhero.service.dto.replay.ReplayDetails
import me.honnold.ladderhero.service.dto.replay.ReplaySummary
import me.honnold.ladderhero.util.gameDuration
import me.honnold.ladderhero.util.isUUID
import me.honnold.ladderhero.util.toUUID
import me.honnold.ladderhero.util.windowsTimeToDate
import me.honnold.s2protocol.model.data.Blob
import me.honnold.s2protocol.model.data.Struct
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.ZoneOffset

@Service
class ReplayService(private val replayDAO: ReplayDAO) {
    companion object {
        private val logger = LoggerFactory.getLogger(ReplayService::class.java)
        private val slugger = Slugify()
    }

    fun getReplays(pageRequest: PageRequest, profileId: Long?): Flux<ReplaySummary> {
        val replaySummaryRows = if (profileId == null)
            this.replayDAO.findAll(pageRequest)
        else
            this.replayDAO.findAllByProfileId(profileId, pageRequest)

        return replaySummaryRows
            .groupBy { row -> row.replayId }
            .flatMap { replayFlux ->
                replayFlux
                    .collectList()
                    .map { replayRows ->
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
                                    player.didWin
                                )
                            }
                        )

                        replaySummary
                    }
            }
            .sort { o1, o2 -> o2.playedAt.compareTo(o1.playedAt) }
    }

    fun getReplay(lookup: String): Mono<ReplayDetails> {
        val replayDetailsRows = if (lookup.isUUID())
            this.replayDAO.findDetailsById(lookup.toUUID())
        else
            this.replayDAO.findDetailsBySlug(lookup)

        return replayDetailsRows
            .collectList()
            .flatMap { details ->
                if (details.size == 0)
                    return@flatMap Mono.empty<ReplayDetails>()

                val replayDetails =
                    ReplayDetails(
                        details[0].replayId,
                        details[0].mapName,
                        details[0].duration,
                        details[0].playedAt,
                        details[0].replaySlug
                    )

                replayDetails.players.addAll(
                    details
                        .groupBy { row -> row.playerId }
                        .values
                        .map { playerSnapshots ->
                            val playerDetails =
                                ReplayDetails.ReplayPlayer(
                                    playerSnapshots[0].playerId,
                                    playerSnapshots[0].race,
                                    playerSnapshots[0].name,
                                    playerSnapshots[0].teamId,
                                    playerSnapshots[0].didWin,
                                    playerSnapshots[0].profileId,
                                    playerSnapshots[0].collectedMinerals,
                                    playerSnapshots[0].collectedVespene,
                                    playerSnapshots[0].avgUnspentMinerals,
                                    playerSnapshots[0].avgUnspentVespene,
                                    playerSnapshots[0].avgCollectionRateMinerals,
                                    playerSnapshots[0].avgCollectionRateVespene
                                )

                            playerDetails.snapshots.addAll(
                                playerSnapshots.map { snapshot ->
                                    ReplayDetails.ReplayPlayer.PlayerSnapshot(
                                        snapshot.loop,
                                        snapshot.lostMinerals,
                                        snapshot.lostVespene,
                                        snapshot.unspentMinerals,
                                        snapshot.unspentVespene,
                                        snapshot.collectionRateMinerals,
                                        snapshot.collectionRateVespene,
                                        snapshot.activeWorkers,
                                        snapshot.armyValueMinerals,
                                        snapshot.armyValueVespene
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
        val mapName = replayData.metadata["Title"].toString()
        val playedAt = windowsTimeToDate(replayData.details["m_timeUTC"])
        val duration = gameDuration(replayData.gameEvents)

        val players: List<Struct> = replayData.details["m_playerList"]
        val matchup = players
            .groupBy { val team: Long = it["m_teamId"]; team }
            .values.joinToString("-") { t ->
                t.joinToString("") { p ->
                    val raceBlob: Blob = p["m_race"]
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

        return this.replayDAO.save(replay)
    }
}