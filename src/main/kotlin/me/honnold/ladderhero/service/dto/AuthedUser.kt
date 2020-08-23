package me.honnold.ladderhero.service.dto

class AuthedUser(
    var username: String,
    var linkedToBlizzard: Boolean = false,
    var battleNetId: Long = -1,
    var battletag: String? = null,
    var sc2Accounts: List<SC2Profile.SC2Account> = emptyList()
)
