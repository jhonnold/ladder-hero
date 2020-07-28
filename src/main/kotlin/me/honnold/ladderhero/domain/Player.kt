package me.honnold.ladderhero.domain

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import java.util.*
import kotlin.collections.HashSet

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