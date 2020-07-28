package me.honnold.ladderhero.dao.domain

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import java.util.*

data class Player(
    @Id
    var id: UUID? = null,
    var profileId: Long,
    var regionId: Long,
    var realmId: Long
) {
    @Transient
    var summaries: List<Summary> = emptyList()
}