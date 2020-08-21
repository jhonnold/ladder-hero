package me.honnold.ladderhero.dao.domain

import java.util.*
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

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
    var profileId: Int = -1,
    @Column("is_admin")
    var isAdmin: Boolean = false,
    @Column("code")
    var code: String? = null)
