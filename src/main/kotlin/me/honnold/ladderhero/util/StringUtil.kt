package me.honnold.ladderhero.util

import org.apache.commons.text.StringEscapeUtils
import java.util.*

fun String.isUUID(): Boolean {
    return try {
        UUID.fromString(this)

        true
    } catch (e: IllegalArgumentException) {
        false
    }
}

fun String.toUUID(): UUID = UUID.fromString(this)

fun unescapeName(name: String): String = StringEscapeUtils.unescapeHtml4(name).replace("<sp/>", " ")
