package me.honnold.ladderhero.service.dto.replay.v2

import java.time.LocalDateTime
import java.util.*

data class ReplayDetails(
    var replayId: UUID? = null,
    var mapName: String? = null,
    var duration: Long = -1,
    var playedAt: LocalDateTime? = null,
    var slug: String? = null,
    var players: Collection<ReplayPlayer> = mutableListOf()
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
    var gameTime: Collection<Long> = mutableListOf()
    var lostResources: Collection<Long> = mutableListOf()
    var unspentResources: Collection<Long> = mutableListOf()
    var collectionRate: Collection<Long> = mutableListOf()
    var activeWorkers: Collection<Long> = mutableListOf()
    var armyValue: Collection<Long> = mutableListOf()
}
