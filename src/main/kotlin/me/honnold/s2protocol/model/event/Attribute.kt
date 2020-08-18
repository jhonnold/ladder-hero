package me.honnold.s2protocol.model.event

class Attribute(val namespace: Int, val id: Int, val scope: Int, val value: String) {
    override fun toString(): String {
        return "Attribute(namespace=$namespace,id=$id,scope=$scope,value=$value)"
    }
}