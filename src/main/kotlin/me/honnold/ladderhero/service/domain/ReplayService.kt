package me.honnold.ladderhero.service.domain

import com.github.slugify.Slugify
import me.honnold.ladderhero.dao.ReplayDAO
import me.honnold.ladderhero.dao.domain.Replay
import me.honnold.ladderhero.service.dto.replay.ReplayData
import me.honnold.ladderhero.service.dto.replay.ReplaySummary
import me.honnold.ladderhero.util.gameDuration
import me.honnold.ladderhero.util.isUUID
import me.honnold.ladderhero.util.toUUID
import me.honnold.ladderhero.util.windowsTimeToDate
import me.honnold.sc2protocol.model.data.Blob
import me.honnold.sc2protocol.model.data.Struct
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
        val replays = if (profileId == null)
            this.replayDAO.findAll(pageRequest)
        else
            this.replayDAO.findAllByProfileId(profileId, pageRequest)

        return replays
            .groupBy { row -> row.replayId }
            .flatMap { replayFlux ->
                replayFlux.reduce(ReplaySummary(), { acc, r ->
                    acc.replayId = r.replayId
                    acc.mapName = r.mapName
                    acc.duration = r.duration
                    acc.playedAt = r.playedAt
                    acc.slug = r.replaySlug

                    acc.players.add(ReplaySummary.ReplayPlayer(r.playerId, r.race, r.name, r.profileId))

                    acc
                })
            }
            .sort { o1, o2 -> o2.playedAt.compareTo(o1.playedAt) }

    }

    fun getReplay(lookup: String): Mono<Replay> {
        return if (lookup.isUUID())
            this.replayDAO.findById(lookup.toUUID())
                .onErrorResume { this.replayDAO.findBySlug(lookup) }
        else
            this.replayDAO.findBySlug(lookup)
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

        return this.replayDAO.save(replay)
    }
}