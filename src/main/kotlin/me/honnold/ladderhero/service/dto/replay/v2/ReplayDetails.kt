package me.honnold.ladderhero.service.dto.replay.v2

import java.time.LocalDateTime
import java.util.*

data class ReplayDetails(
    var replayId: UUID? = null,
    var mapName: String? = null,
    var duration: Long = -1,
    var playedAt: LocalDateTime? = null,
    var slug: String? = null,
    var players: List<ReplayPlayer> = mutableListOf()
)

data class ReplayPlayer(
    var summaryId: UUID? = null,
    var playerId: UUID? = null,
    var race: String? = null,
    var name: String? = null,
    var profileId: Long = -1,
    var teamId: Long = -1,
    var didWin: Boolean = false,
    var mmr: Long = -1,
    var totalLostResources: Long = -1,
    var totalCollectedResources: Long = -1,
    var avgUnspentResources: Long = -1,
    var avgCollectionRate: Long = -1
) {
    var gameTime: List<Long> = mutableListOf()
    var lostResources: List<Long> = mutableListOf()
    var unspentResources: List<Long> = mutableListOf()
    var collectionRate: List<Long> = mutableListOf()
    var activeWorkers: List<Long> = mutableListOf()
    var armyValue: List<Long> = mutableListOf()
}
