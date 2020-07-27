package me.honnold.ladderhero.repository

import me.honnold.ladderhero.domain.Replay
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import java.util.*

interface ReplayRepository : ReactiveCrudRepository<Replay, UUID>