package me.honnold.mpq.model

import java.nio.ByteBuffer

class Header(val offset: Int, val magic: ByteBuffer, val headerSize: Int, val archiveSize: Int, val formatVersion: Int,
             val sectorSizeShift: Int, val hashTableOffset: Int, val blockTableOffset: Int, val hashTableEntries: Int, val blockTableEntries: Int)