package me.honnold.ladderhero.repository

import me.honnold.ladderhero.domain.Replay
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import java.util.*

interface ReplayRepository : ReactiveCrudRepository<Replay, UUID> {
    @Query("select * from replay order by played_at desc limit :size offset :offset")
    fun findPage(size: Int, offset: Long): Flux<Replay>
}