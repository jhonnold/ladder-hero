package me.honnold.ladderhero.repository

import me.honnold.ladderhero.dao.domain.SummarySnapshot
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import java.util.*

interface SummarySnapshotRepository : ReactiveCrudRepository<SummarySnapshot, UUID> {
    @Query("select * from summary_snapshot where summary_id = :summaryId")
    fun findAllBySummaryId(summaryId: UUID): Flux<SummarySnapshot>
}