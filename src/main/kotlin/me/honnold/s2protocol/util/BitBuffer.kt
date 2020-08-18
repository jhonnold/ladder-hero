package me.honnold.s2protocol.util

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.min

class BitBuffer(private val buffer: ByteBuffer, private val order: ByteOrder) {
    private var next = 0L
    private var nextBits = 0

    fun align() {
        this.nextBits = 0
    }

    fun hasRemaining() = this.nextBits != 0 || buffer.hasRemaining()

    fun read(count: Int): Long {
        if (count == 0) return 0

        var result = 0L
        var resultBits = 0

        while (resultBits != count) {
            if (this.nextBits == 0) {
                this.next = this.buffer.get().toLong() and 0xFFL
                this.nextBits = 8
            }

            val copyBits = min(this.nextBits, count - resultBits)
            val copy = this.next and ((1L shl copyBits) - 1)

            result = if (order == ByteOrder.BIG_ENDIAN) {
                result or (copy shl (count - resultBits - copyBits))
            } else {
                result or (copy shl resultBits)
            }

            this.next = this.next shr copyBits
            this.nextBits -= copyBits
            resultBits += copyBits
        }

        return result
    }

    fun readBytes(count: Int): ByteBuffer {
        val data = ByteArray(count)
        val buffer = ByteBuffer.wrap(data)

        (0 until count).forEach { data[it] = (this.read(8) and 0xFF).toByte() }

        return buffer
    }
}