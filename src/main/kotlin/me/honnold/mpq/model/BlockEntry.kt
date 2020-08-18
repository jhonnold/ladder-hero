package me.honnold.mpq.model

class BlockEntry(val offset: Int, val archivedSize: Int, val size: Int, val flags: Int) {
    companion object {
        const val SIZE = 16
    }

    override fun toString(): String {
        return "%08x %08x %08x %08x".format(offset, archivedSize, size, flags)
    }
}