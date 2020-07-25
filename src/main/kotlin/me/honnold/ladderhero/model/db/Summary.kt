package me.honnold.ladderhero.model.db

import org.springframework.data.annotation.Id
import java.util.*

data class Summary(
    @Id
    var id: UUID? = null,

    var replayId: UUID?,
    var playerId: UUID?,

    var workingId: Int,
    var race: String,
    var name: String
)