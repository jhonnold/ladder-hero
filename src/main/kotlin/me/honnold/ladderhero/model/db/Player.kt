package me.honnold.ladderhero.model.db

import org.springframework.data.annotation.Id
import java.util.*

data class Player(
    @Id
    var id: UUID? = null,
    var profileId: Int,
    var regionId: Int,
    var realmId: Int
)