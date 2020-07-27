package me.honnold.ladderhero.repository

import me.honnold.ladderhero.domain.Summary
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import java.util.*

interface SummaryRepository : ReactiveCrudRepository<Summary, UUID>