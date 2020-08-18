package me.honnold.s2protocol.model.event

class AttributeEvents(val source: Int, val mapNamespace: Int) {
    val scopes: MutableMap<Int, MutableMap<Int, Attribute>> = HashMap()

    fun addToScope(scopeId: Int, attribute: Attribute) {
        var scope = this.scopes[scopeId]
        if (scope == null) {
            scope = HashMap()
            this.scopes[scopeId] = scope
        }

        scope[attribute.id] = attribute
    }

    override fun toString(): String {
        return "AttributeEvents(source=$source, mapNamespace=$mapNamespace, scopes=$scopes)"
    }
}
