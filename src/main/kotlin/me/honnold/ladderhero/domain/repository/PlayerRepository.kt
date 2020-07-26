package me.honnold.ladderhero.domain.repository

import me.honnold.ladderhero.domain.model.Player
import org.springframework.data.repository.CrudRepository
import java.util.*

interface PlayerRepository : CrudRepository<Player, UUID> {
    fun existsByProfileId(profileId: Long): Boolean
    fun findByProfileId(profileId: Long): Player
}