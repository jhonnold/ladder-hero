package me.honnold.s2protocol.model.type

class Bounds(val offset: Long, val bits: Int) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Bounds

        return other.offset == this.offset && other.bits == this.bits
    }

    override fun hashCode(): Int = this.offset.hashCode() + this.bits.hashCode()

    override fun toString(): String {
        return "(%d,%d)".format(offset, bits)
    }
}