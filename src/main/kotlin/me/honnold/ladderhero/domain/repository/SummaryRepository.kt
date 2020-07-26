package me.honnold.ladderhero.domain.repository

import me.honnold.ladderhero.domain.model.Summary
import org.springframework.data.repository.CrudRepository
import java.util.*

interface SummaryRepository : CrudRepository<Summary, UUID>