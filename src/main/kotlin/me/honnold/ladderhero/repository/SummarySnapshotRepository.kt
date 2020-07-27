package me.honnold.ladderhero.repository

import me.honnold.ladderhero.domain.SummarySnapshot
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import java.util.*

interface SummarySnapshotRepository : ReactiveCrudRepository<SummarySnapshot, UUID>