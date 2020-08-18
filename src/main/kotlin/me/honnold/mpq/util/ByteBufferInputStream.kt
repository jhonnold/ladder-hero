package me.honnold.mpq.util

import java.io.InputStream
import java.nio.ByteBuffer

class ByteBufferInputStream(private val buffer: ByteBuffer): InputStream() {
    @ExperimentalUnsignedTypes @Synchronized
    override fun read() = if (this.buffer.hasRemaining()) this.buffer.get().toUByte().toInt() else -1
}