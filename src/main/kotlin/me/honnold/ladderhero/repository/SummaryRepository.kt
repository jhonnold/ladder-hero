package me.honnold.ladderhero.repository

import me.honnold.ladderhero.model.db.Replay
import me.honnold.ladderhero.model.db.Summary
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import java.util.*

interface SummaryRepository : ReactiveCrudRepository<Summary, UUID>