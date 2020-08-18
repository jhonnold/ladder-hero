package me.honnold.s2protocol.model.type

class ArrayTypeInfo(bounds: Bounds, type: Int) : TypeInfo(TypeMethod.ARRAY) {
    override val p: Bounds = bounds
    override val q: Int = type

    override fun toString(): String {
        return "('_array',[${this.p}, ${this.q}])"
    }
}