package me.honnold.ladderhero.dao.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("users")
data class User(
    @Id
    @Column("id")
    var id: UUID? = null,
    @Column("username")
    var username: String,
    @Column("password")
    var encodedPassword: String,
    @Column("profile_id")
    var profileId: Long = -1,
    @Column("is_admin")
    var isAdmin: Boolean = false,
    @Column("account_id")
    var accountId: Long = -1,
    @Column("battle_tag")
    var battleTag: String? = null
)
