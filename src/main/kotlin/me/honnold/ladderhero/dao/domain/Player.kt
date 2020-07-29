package me.honnold.ladderhero.dao.domain

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Column
import java.util.*

data class Player(
    @Id
    @Column("id")
    var id: UUID? = null,

    @Column("profile_id")
    var profileId: Long,

    @Column("region_id")
    var regionId: Long,

    @Column("realm_id")
    var realmId: Long
)