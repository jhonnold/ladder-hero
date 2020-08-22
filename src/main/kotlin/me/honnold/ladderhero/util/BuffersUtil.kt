package me.honnold.ladderhero.util

import org.slf4j.LoggerFactory
import org.springframework.core.io.buffer.DataBuffer
import java.nio.ByteBuffer

class BuffersUtil {
    companion object {
        private val logger = LoggerFactory.getLogger(BuffersUtil::class.java)

        fun concatBuffers(buffers: List<DataBuffer>): ByteBuffer {
            logger.debug("Creating single buffer from ${buffers.size} chunks")

            val size = buffers.map { it.readableByteCount() }.sum()
            val data = ByteBuffer.allocate(size)
            buffers.forEach { data.put(it.asByteBuffer()) }

            data.rewind()
            logger.debug("Generated final buffer of size ${data.capacity()}")
            return data
        }
    }
}
