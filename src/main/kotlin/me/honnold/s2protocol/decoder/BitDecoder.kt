package me.honnold.s2protocol.decoder

import me.honnold.s2protocol.model.data.BitArray
import me.honnold.s2protocol.model.data.Blob
import me.honnold.s2protocol.model.data.Struct
import me.honnold.s2protocol.model.type.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

class BitDecoder(infos: List<TypeInfo>, buffer: ByteBuffer, order: ByteOrder = ByteOrder.BIG_ENDIAN) :
    Decoder(infos, buffer, order) {

    override fun getArray(bounds: Bounds, id: Int): List<Any?> {
        val length = this.getNumber(bounds)

        return (0 until length).map {
            when (val info = this.infos[id]) {
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
    }

    override fun getBitArray(bounds: Bounds): BitArray {
        val length = this.getNumber(bounds).toInt()

        val bits = BitArray(length)
        (0 until length).forEach { bits.set(it, this.input.read(1) != 0L) }

        return bits
    }

    override fun getBlob(bounds: Bounds): Blob {
        val length = this.getNumber(bounds).toInt()

        this.input.align()
        return Blob(this.input.readBytes(length))
    }

    override fun getBool(): Boolean {
        return this.input.read(1) != 0L
    }

    override fun getChoice(bounds: Bounds, fields: List<Field>): Pair<String, Any?> {
        val tag = this.getNumber(bounds).toInt()
        val field = fields[tag]

        return Pair(field.name, this.get(field.typeId))
    }

    override fun getFourCC(): Long {
        this.input.align()

        return this.input.readBytes(4).int.toLong()
    }

    override fun getNumber(bounds: Bounds): Long {
        return bounds.offset + this.input.read(bounds.bits)
    }

    override fun getOptional(id: Int): Any? {
        return if (this.getBool()) this.get(id) else null
    }

    override fun getStruct(fields: List<Field>): Struct {
        val result = HashMap<String, Any?>()

        fields.forEach { result[it.name] = this.get(it.typeId) }

        return Struct(result)
    }
}
