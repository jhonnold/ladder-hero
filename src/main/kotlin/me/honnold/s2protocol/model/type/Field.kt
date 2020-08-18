package me.honnold.s2protocol.model.type

class Field(val name: String, val typeId: Int, val tag: Int = 0) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Field

        return other.name == this.name && other.typeId == this.typeId && other.tag == this.tag
    }

    override fun hashCode(): Int = this.name.hashCode() + this.typeId.hashCode() + this.tag.hashCode()

    override fun toString(): String {
        return "('%s',%d,%d)".format(name, typeId, tag)
    }
}