package me.honnold.s2protocol.util

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.test.Test
import kotlin.test.assertEquals

class BitBufferTest {
    @Test
    fun readingBits() {
        val buffer = ByteBuffer.allocate(2)
        buffer.put(6.toByte())
        buffer.put((-3).toByte())

        buffer.rewind()

        val bitBuffer = BitBuffer(buffer, ByteOrder.BIG_ENDIAN)

        val a = bitBuffer.read(1)
        assertEquals(0, a)

        val b = bitBuffer.read(1)
        assertEquals(1, b)

        val c = bitBuffer.read(8)
        assertEquals(5, c)
    }

    @Test
    fun readingBits_little() {
        val buffer = ByteBuffer.allocate(2)
        buffer.put(6.toByte())
        buffer.put((-3).toByte())

        buffer.rewind()

        val bitBuffer = BitBuffer(buffer, ByteOrder.LITTLE_ENDIAN)

        val a = bitBuffer.read(1)
        assertEquals(0, a)

        val b = bitBuffer.read(1)
        assertEquals(1, b)

        val c = bitBuffer.read(8)
        assertEquals(65, c)
    }
}