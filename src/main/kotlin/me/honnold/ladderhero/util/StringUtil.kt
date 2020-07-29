package me.honnold.ladderhero.util

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
