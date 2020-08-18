package me.honnold.s2protocol.model.data

class Struct(val struct: Map<String, Any?>) {
    inline operator fun <reified T> get(key: String): T {
        val value =
            this.struct[key] ?: throw NullPointerException("$key is not on this struct. Keys are ${this.struct.keys}")

        if (value !is T) throw ClassCastException("Value is not of type ${T::class.simpleName}, it is ${value::class.simpleName}")
        return value
    }

    override fun toString(): String {
        return struct.toString()
    }
}