package me.honnold.s2protocol.model.data

import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class Blob(buffer: ByteBuffer, charset: Charset = StandardCharsets.UTF_8) {
    val value = charset.decode(buffer).toString()

    override fun toString() = this.value
}