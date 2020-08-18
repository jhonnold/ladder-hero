package me.honnold.s2protocol.decoder

import me.honnold.s2protocol.model.data.BitArray
import me.honnold.s2protocol.model.data.Blob
import me.honnold.s2protocol.model.data.Struct
import me.honnold.s2protocol.model.type.*
import me.honnold.s2protocol.util.BitBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

abstract class Decoder(val infos: List<TypeInfo>, buffer: ByteBuffer, order: ByteOrder = ByteOrder.BIG_ENDIAN) {
    val input = BitBuffer(buffer, order)

    fun get(id: Int): Any? {
        return when (val info = this.infos[id]) {
            is ArrayTypeInfo -> getArray(info.p, info.q)
            is BitArrayTypeInfo -> getBitArray(info.p)
            is BlobTypeInfo -> getBlob(info.p)
            is BoolTypeInfo -> getBool()
            is ChoiceTypeInfo -> getChoice(info.p, info.q)
            is FourCCTypeInfo -> getFourCC()
            is NumberTypeInfo -> getNumber(info.p)
            is NullTypeInfo -> null
            is OptionalTypeInfo -> getOptional(info.p)
            is StructTypeInfo -> getStruct(info.p)
            else -> UnsupportedOperationException("Unable to process $info")
        }
    }

    abstract fun getArray(bounds: Bounds, id: Int): List<Any?>
    abstract fun getBitArray(bounds: Bounds): BitArray
    abstract fun getBlob(bounds: Bounds): Blob
    abstract fun getBool(): Boolean
    abstract fun getChoice(bounds: Bounds, fields: List<Field>): Pair<String, Any?>
    abstract fun getFourCC(): Long
    abstract fun getNumber(bounds: Bounds): Long
    abstract fun getOptional(id: Int): Any?
    abstract fun getStruct(fields: List<Field>): Struct
}