package me.honnold.ladderhero.model.db

import org.apache.commons.text.StringEscapeUtils
import org.springframework.data.annotation.Id
import java.util.*

data class Player(
    @Id
    var id: UUID? = null,
    var profileId: Int,
    var regionId: Int,
    var realmId: Int,
    var name: String
) {
    init {
        this.name = StringEscapeUtils.unescapeHtml4(name).replace("<sp/>", " ")
    }
}