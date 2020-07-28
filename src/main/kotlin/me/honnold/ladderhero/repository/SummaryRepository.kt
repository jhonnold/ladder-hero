package me.honnold.ladderhero.repository

import me.honnold.ladderhero.dao.domain.Summary
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import java.util.*

interface SummaryRepository : ReactiveCrudRepository<Summary, UUID> {
    @Query("select * from summary where replay_id = :replayId")
    fun getSummariesForReplayId(replayId: UUID): Flux<Summary>
}