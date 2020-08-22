package me.honnold.ladderhero.dao.value

import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime
import java.util.*

/** Class to represent the output of a join query */
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
    @Column("team_id")
    var teamId: Long,
    @Column("did_win")
    var didWin: Boolean,
    @Column("profile_id")
    var profileId: Long,
    @Column("region_id")
    var regionId: Long,
    @Column("realm_id")
    var realmId: Long
) {
    companion object {
        const val ALL_QUERY =
            "select * from " +
                "(select *, dense_rank() over (order by played_at desc) offset_ from " +
                "(select *, s.id as summary_id from replay r, summary s, player p " +
                "where r.id = s.replay_id and s.player_id = p.id " +
                "order by played_at desc " +
                ") res " +
                ") res_offset " +
                "where offset_ > :offset and offset_ <= :offset + :limit"

        const val PROFILE_ID_QUERY =
            "with rs as (select r.id " +
                "from replay r, summary s, player p " +
                "where r.id = s.replay_id and s.player_id = p.id and p.profile_id = :profileId  " +
                "order by r.played_at desc offset :offset limit :limit) " +
                "select *, s.id as summary_id from replay r, summary s, player p " +
                "where r.id in (select id from rs) and r.id = s.replay_id and s.player_id = p.id"
    }
}
