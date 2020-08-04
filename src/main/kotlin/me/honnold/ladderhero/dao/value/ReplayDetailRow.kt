package me.honnold.ladderhero.dao.value

import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime
import java.util.*

/**
 * Class to represent the output of a join
 * query
 */
data class ReplayDetailRow(
    @Column("replay_id")
    val replayId: UUID,

    @Column("map_nm")
    val mapName: String,

    @Column("dur_s")
    val duration: Long,

    @Column("played_at")
    val playedAt: LocalDateTime,

    @Column("slug")
    val replaySlug: String,

    @Column("summary_id")
    var summaryId: UUID,

    @Column("race")
    var race: String,

    @Column("name")
    var name: String,

    @Column("player_id")
    var playerId: UUID,

    @Column("team_id")
    var teamId: Long,

    @Column("did_win")
    var didWin: Boolean,

    @Column("profile_id")
    var profileId: Long,

    @Column("region_id")
    var regionId: Long,

    @Column("realm_id")
    var realmId: Long,

    @Column("loop")
    var loop: Long,

    @Column("collected_minerals")
    var collectedMinerals: Long,

    @Column("collected_vespene")
    var collectedVespene: Long,

    @Column("avg_unspent_minerals")
    var avgUnspentMinerals: Long,

    @Column("avg_unspent_vespene")
    var avgUnspentVespene: Long,

    @Column("avg_collection_rate_minerals")
    var avgCollectionRateMinerals: Long,

    @Column("avg_collection_rate_vespene")
    var avgCollectionRateVespene: Long,

    @Column("lost_minerals")
    var lostMinerals: Long,

    @Column("lost_vespene")
    var lostVespene: Long,

    @Column("unspent_minerals")
    var unspentMinerals: Long,

    @Column("unspent_vespene")
    var unspentVespene: Long,

    @Column("collection_rate_minerals")
    var collectionRateMinerals: Long,

    @Column("collection_rate_vespene")
    var collectionRateVespene: Long,

    @Column("active_workers")
    var activeWorkers: Long,

    @Column("army_value_minerals")
    var armyValueMinerals: Long,

    @Column("army_value_vespene")
    var armyValueVespene: Long
) {
    companion object {
        const val ID_QUERY =
            "select * from replay r, summary s, player p, summary_snapshot ss " +
                    "where r.id = s.replay_id and s.player_id = p.id and s.id = ss.summary_id  " +
                    "and r.id = :id"

        const val SLUG_QUERY =
            "select * from replay r, summary s, player p, summary_snapshot ss " +
                    "where r.id = s.replay_id and s.player_id = p.id and s.id = ss.summary_id  " +
                    "and r.slug = :slug"
    }
}