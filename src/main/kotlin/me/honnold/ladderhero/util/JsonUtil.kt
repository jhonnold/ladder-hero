package me.honnold.ladderhero.util

import io.r2dbc.postgresql.codec.Json
import org.json.simple.JSONAware
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser

fun Json.toJSONObject(): JSONObject {
    val parser = JSONParser()

    return parser.parse(this.asString()) as JSONObject
}

fun JSONAware.toJson(): Json {
    return Json.of(this.toJSONString())
}