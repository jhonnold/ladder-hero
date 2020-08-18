package me.honnold.s2protocol.decoder

import me.honnold.s2protocol.model.data.BitArray
import me.honnold.s2protocol.model.data.Blob
import me.honnold.s2protocol.model.data.Struct
import me.honnold.s2protocol.model.type.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

class VersionedBitDecoder(
    infos: List<TypeInfo>,
    buffer: ByteBuffer,
    order: ByteOrder = ByteOrder.BIG_ENDIAN
) : Decoder(infos, buffer, order) {
    private fun getVNumber(): Long {
        var value = 0L
        var shift = 0

        while (true) {
            val data = this.input.read(8)

            value = value or ((data and 0x7FL) shl shift)
            if (data and 0x80L == 0L)
                return if (value and 0x01 > 0) -(value shr 1) else value shr 1

            shift += 7
        }
    }

    private fun skip() {
        when (this.input.read(8).toInt()) {
            0 -> {
                val length = this.getVNumber()

                (0 until length).forEach { _ -> this.skip() }
            }
            1 -> {
                val skip = this.getVNumber().toInt()

                this.input.align()
                this.input.readBytes(skip and 0x1FFF_FFFF)
            }
            2 -> {
                val skip = this.getVNumber().toInt()

                this.input.align()
                this.input.readBytes(skip and 0x1FFF_FFFF)
            }
            3 -> {
                this.getVNumber()

                this.skip()
            }
            4 -> {
                if (this.input.read(8) != 0L) this.skip()
            }
            5 -> {
                val length = this.getVNumber()
                (0 until length).forEach { _ ->
                    this.getVNumber()
                    this.skip()
                }
            }
            6 -> {
                this.input.align()
                this.input.readBytes(1)
            }
            7 -> {
                this.input.align()
                this.input.readBytes(4)
            }
            8 -> {
                this.input.align()
                this.input.readBytes(8)
            }
            9 -> {
                this.getVNumber()
            }
        }
    }

    override fun getArray(bounds: Bounds, id: Int): List<Any?> {
        this.input.read(8)

        val length = this.getVNumber()

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
        this.input.read(8)

        val length = this.getVNumber().toInt()
        val bits = BitArray(length)

        this.input.align()
        (0 until length).forEach { bits.set(it, this.input.read(1) != 0L) }

        return bits
    }

    override fun getBlob(bounds: Bounds): Blob {
        this.input.read(8)

        val length = this.getVNumber().toInt()

        this.input.align()
        return Blob(this.input.readBytes(length))
    }

    override fun getBool(): Boolean {
        this.input.read(8)

        return this.input.read(8) != 0L
    }

    override fun getChoice(bounds: Bounds, fields: List<Field>): Pair<String, Any?> {
        this.input.read(8)

        val tag = this.getVNumber().toInt()
        val field = fields[tag]

        return Pair(field.name, this.get(field.typeId))
    }

    override fun getFourCC(): Long {
        this.input.read(8)

        this.input.align()
        return this.input.readBytes(4).int.toLong()
    }

    override fun getNumber(bounds: Bounds): Long {
        this.input.read(8)

        return this.getVNumber()
    }

    override fun getOptional(id: Int): Any? {
        this.input.read(8)

        return if (this.input.read(8) != 0L) this.get(id) else null
    }

    override fun getStruct(fields: List<Field>): Struct {
        this.input.read(8)

        val length = this.getVNumber()
        val result = HashMap<String, Any?>()

        (0 until length).forEach { _ ->
            val tag = this.getVNumber().toInt()

            val field = fields.findLast { it.tag == tag }
            if (field == null) {
                this.skip()
            } else {
                result[field.name] = this.get(field.typeId)
            }
        }

        return Struct(result)
    }


}