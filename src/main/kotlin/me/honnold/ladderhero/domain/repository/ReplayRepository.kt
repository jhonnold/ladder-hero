package me.honnold.ladderhero.domain.repository

import me.honnold.ladderhero.domain.model.Replay
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.*

interface ReplayRepository : PagingAndSortingRepository<Replay, UUID>