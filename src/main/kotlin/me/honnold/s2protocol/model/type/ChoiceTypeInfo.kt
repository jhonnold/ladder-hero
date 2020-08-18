package me.honnold.s2protocol.model.type

class ChoiceTypeInfo(bounds: Bounds, fields: List<Field>) : TypeInfo(TypeMethod.CHOICE) {
    override val p: Bounds = bounds
    override val q: List<Field> = fields

    override fun toString(): String {
        return "('_choice',[${this.p}, ${this.q}])"
    }
}