package me.honnold.ladderhero.domain.repository

import me.honnold.ladderhero.domain.model.Replay
import org.springframework.data.repository.CrudRepository
import java.util.*

interface ReplayRepository : CrudRepository<Replay, UUID>