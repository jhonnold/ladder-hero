package me.honnold.ladderhero.dao.value

import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime
import java.util.*

/**
 * Class to represent the output of a join
 * query
 */
data class ReplaySummaryRow(
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

    @Column("race")
    var race: String,

    @Column("name")
    var name: String,

    @Column("player_id")
    var playerId: UUID,

    @Column("profile_id")
    var profileId: Long,

    @Column("region_id")
    var regionId: Long,

    @Column("realm_id")
    var realmId: Long
)