package me.honnold.s2protocol.model.type

class OptionalTypeInfo(type: Int) : TypeInfo(TypeMethod.OPTIONAL) {
    override val p: Int = type

    override fun toString(): String {
        return "('_optional',[${this.p}])"
    }
}