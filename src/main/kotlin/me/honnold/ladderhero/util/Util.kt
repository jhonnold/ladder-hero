package me.honnold.ladderhero.util

import me.honnold.sc2protocol.model.event.Event
import org.apache.commons.text.StringEscapeUtils
import java.util.*
import kotlin.math.floor

fun windowsTimeToDate(time: Long): Date {
    val epoch = time / 10_000 - 11_644_473_600_000

    return Date(epoch)
}

fun gameDuration(events: List<Event>): Int {
    val firstLeaveEvent = events.find { it.name == "NNet.Game.SGameUserLeaveEvent" }

    return if (firstLeaveEvent == null) 0 else floor(firstLeaveEvent.loop / 22.4).toInt()
}

fun unescapeName(name: String): String = StringEscapeUtils.unescapeHtml4(name).replace("<sp/>", " ")