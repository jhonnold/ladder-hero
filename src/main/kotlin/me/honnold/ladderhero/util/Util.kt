package me.honnold.ladderhero.util

import me.honnold.s2protocol.model.data.Struct
import me.honnold.s2protocol.model.event.Event
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.math.floor

fun windowsTimeToDate(time: Long): LocalDateTime {
    val epoch = time / 10_000_000 - 11_644_473_600

    return LocalDateTime.ofEpochSecond(epoch, 0, ZoneOffset.UTC)
}

fun gameDuration(events: List<Event>): Long {
    val firstLeaveEvent = events.find { it.name == "NNet.Game.SGameUserLeaveEvent" }

    return if (firstLeaveEvent == null) 0 else floor(firstLeaveEvent.loop / 22.4).toLong()
}

fun Struct.getLong(key: String): Long = this[key]
