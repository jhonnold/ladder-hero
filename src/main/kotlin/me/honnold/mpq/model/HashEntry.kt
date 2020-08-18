package me.honnold.mpq.model

class HashEntry(val fileHashA: Int, val fileHashB: Int, val language: Short, val platform: Short, val fileBlock: Int) {
    companion object {
        const val SIZE = 16
    }

    override fun toString(): String {
        return "%08x %08x %04x %04x %08x".format(fileHashA, fileHashB, language, platform, fileBlock)
    }
}