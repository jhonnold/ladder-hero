package me.honnold.s2protocol.model.type

class BlobTypeInfo(bounds: Bounds) : TypeInfo(TypeMethod.BLOB) {
    override val p: Bounds = bounds

    override fun toString(): String {
        return "('_blob',[${this.p}])"
    }
}