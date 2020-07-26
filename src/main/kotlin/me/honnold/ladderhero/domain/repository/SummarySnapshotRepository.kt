package me.honnold.ladderhero.domain.repository

import me.honnold.ladderhero.domain.model.SummarySnapshot
import org.springframework.data.repository.CrudRepository
import java.util.*

interface SummarySnapshotRepository : CrudRepository<SummarySnapshot, UUID>