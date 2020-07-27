package me.honnold.ladderhero.domain

import org.springframework.data.annotation.Id
import java.util.*

data class Player(
    @Id
    var id: UUID? = null,
    var profileId: Long,
    var regionId: Long,
    var realmId: Long
)