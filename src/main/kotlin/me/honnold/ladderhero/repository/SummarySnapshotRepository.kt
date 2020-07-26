package me.honnold.ladderhero.repository

import me.honnold.ladderhero.model.db.FileUpload
import me.honnold.ladderhero.model.db.SummarySnapshot
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import java.util.*

interface SummarySnapshotRepository : ReactiveCrudRepository<SummarySnapshot, UUID>