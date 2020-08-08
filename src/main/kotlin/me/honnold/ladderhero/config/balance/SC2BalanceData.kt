package me.honnold.ladderhero.config.balance

import org.json.simple.JSONArray
import org.json.simple.JSONObject

class SC2BalanceData(data: JSONObject) {
    val locale: Map<Long, String>
    val units: Map<String, SC2Unit>

    init {
        val localeArray = data["locale"] as JSONArray
        this.locale = localeArray.map { localeAny ->
            val localeObj = localeAny as JSONObject

            localeObj["id"] as Long to (localeObj["text"] as String?).toString()
        }.toMap()


        val unitsArray = data["units"] as JSONArray
        this.units = unitsArray.map { unitAny ->
            val unitObj = unitAny as JSONObject

            unitObj["id"] as String to SC2Unit(unitObj, this.locale)
        }.toMap()
    }

    class SC2Unit(unitData: JSONObject, locale: Map<Long, String>) {
        val name: String
        val icon: String?
        val costMinerals: Long
        val costVespene: Long
        val costSupply: Long

        init {
            val meta = unitData["meta"] as JSONObject
            this.name = locale[meta["name"] as Long].toString()
            this.icon = meta["icon"] as String?

            val cost = unitData["cost"] as JSONObject?
            if (cost == null) {
                this.costMinerals = 0
                this.costVespene = 0
                this.costSupply = 0
            } else {
                val minerals = cost["minerals"] as Long?
                this.costMinerals = minerals ?: 0

                val vespene = cost["vespene"] as Long?
                this.costVespene = vespene ?: 0

                val supply = cost["supply"] as Long?
                this.costSupply = supply ?: 0
            }
        }
    }
}