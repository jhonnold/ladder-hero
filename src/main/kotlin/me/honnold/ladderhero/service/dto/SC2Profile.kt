package me.honnold.ladderhero.service.dto

class SC2Profile {
    var id: Int = -1
    var battletag: String = ""
    var sc2Accounts = listOf<SC2Account>()

    class SC2Account {
        var name: String = ""
        var profileUrl: String = ""
        var avatarUrl: String = ""
        var profileId: String = ""
        var regionId: Int = -1
        var realmId: Int = -1
    }
}
