package me.honnold.s2protocol.model.type

class StructTypeInfo(fields: List<Field>) : TypeInfo(TypeMethod.STRUCT) {
    override val p: List<Field> = fields

    override fun toString(): String {
        return "('_struct',[${this.p}])"
    }
}