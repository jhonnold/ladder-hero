package me.honnold.ladderhero.repository

import me.honnold.ladderhero.domain.Player
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import java.util.*

interface PlayerRepository : ReactiveCrudRepository<Player, UUID> {
    @Query("select count(*) > 0 from player where profile_id = :profileId")
    fun existsByProfileId(profileId: Long): Mono<Boolean>

    @Query("select * from player where profile_id = :profileId")
    fun findByProfileId(profileId: Long): Mono<Player>
}