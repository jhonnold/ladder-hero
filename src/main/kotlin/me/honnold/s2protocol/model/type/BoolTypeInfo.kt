package me.honnold.s2protocol.model.type

class BoolTypeInfo : TypeInfo(TypeMethod.BOOL) {
    override fun toString(): String {
        return "('_blob',[])"
    }
}