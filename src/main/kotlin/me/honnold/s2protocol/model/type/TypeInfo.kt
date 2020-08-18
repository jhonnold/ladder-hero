package me.honnold.s2protocol.model.type

abstract class TypeInfo(open val method: TypeMethod) {
    open val p: Any? = null
    open val q: Any? = null

    companion object {
        private val METHOD_REGEX = Regex("^\\('(_[a-z]+)'")
        private val BOUNDS_REGEX = Regex("\\((-?[\\d]+),(-?[\\d]+)\\)")
        private val FIELD_REGEX = Regex("\\('([a-zA-Z_]+)',(-?[\\d]+),(-?[\\d]+)\\)")
        private val CHOICE_FIELD_REGEX = Regex("[\\d]+:\\('([\\w]+)',(-?[\\d]+)\\)")
        private val OPTIONAL_REGEX = Regex("\\[(-?[\\d]+)]")

        fun from(src: String): TypeInfo {
            val methodMatch = METHOD_REGEX.find(src)
                ?: throw IllegalArgumentException("$src is not an understood input for TypeInfo")

            val methodGroup = methodMatch.groups[1]
            return when (methodGroup!!.value) {
                "_int" -> {
                    val boundsMatch = BOUNDS_REGEX.find(src, methodGroup.range.last)
                        ?: throw IllegalArgumentException("Unable to parse bounds for _int from $src")

                    val offset = boundsMatch.groups[1]!!.value.toLong()
                    val bits = boundsMatch.groups[2]!!.value.toInt()

                    return NumberTypeInfo(Bounds(offset, bits))
                }
                "_struct" -> {
                    val fieldsMatch = FIELD_REGEX.findAll(src, methodGroup.range.last)
                    val fields = fieldsMatch.map {
                        Field(
                            it.groups[1]!!.value,
                            it.groups[2]!!.value.toInt(),
                            it.groups[3]!!.value.toInt()
                        )
                    }.toList()

                    return StructTypeInfo(fields)
                }
                "_choice" -> {
                    val boundsMatch = BOUNDS_REGEX.find(src, methodGroup.range.last)
                        ?: throw IllegalArgumentException("Unable to parse bounds for _choice from $src")

                    val offset = boundsMatch.groups[1]!!.value.toLong()
                    val bits = boundsMatch.groups[2]!!.value.toInt()
                    val bounds = Bounds(offset, bits)

                    val choiceFieldsMatch = CHOICE_FIELD_REGEX.findAll(src, boundsMatch.range.last)
                    val fields =
                        choiceFieldsMatch.map {
                            Field(
                                it.groups[1]!!.value,
                                it.groups[2]!!.value.toInt()
                            )
                        }.toList()

                    return ChoiceTypeInfo(bounds, fields)
                }
                "_array" -> {
                    val boundsMatch = BOUNDS_REGEX.find(src, methodGroup.range.last)
                        ?: throw IllegalArgumentException("Unable to parse bounds for _array from $src")

                    val offset = boundsMatch.groups[1]!!.value.toLong()
                    val bits = boundsMatch.groups[2]!!.value.toInt()
                    val bounds = Bounds(offset, bits)

                    val typeMatch = Regex("(\\d+)").find(src, boundsMatch.range.last)
                        ?: throw IllegalArgumentException("Unable to parse type for _array from $src")
                    val type = typeMatch.groups[1]!!.value.toInt()

                    return ArrayTypeInfo(bounds, type)
                }
                "_bitarray" -> {
                    val boundsMatch = BOUNDS_REGEX.find(src, methodGroup.range.last)
                        ?: throw IllegalArgumentException("Unable to parse bounds for _bitarray from $src")

                    val offset = boundsMatch.groups[1]!!.value.toLong()
                    val bits = boundsMatch.groups[2]!!.value.toInt()

                    return BitArrayTypeInfo(Bounds(offset, bits))
                }
                "_blob" -> {
                    val boundsMatch = BOUNDS_REGEX.find(src, methodGroup.range.last)
                        ?: throw IllegalArgumentException("Unable to parse bounds for _blob from $src")

                    val offset = boundsMatch.groups[1]!!.value.toLong()
                    val bits = boundsMatch.groups[2]!!.value.toInt()

                    return BlobTypeInfo(Bounds(offset, bits))
                }
                "_optional" -> {
                    val optionalMatch = OPTIONAL_REGEX.find(src, methodGroup.range.last)
                        ?: throw java.lang.IllegalArgumentException("Unable to parse typeId for _optional from $src")

                    val type = optionalMatch.groups[1]!!.value.toInt()

                    return OptionalTypeInfo(type)
                }
                "_bool" -> BoolTypeInfo()
                "_fourcc" -> FourCCTypeInfo()
                "_null" -> NullTypeInfo()
                else -> throw IllegalArgumentException("Unsupported method!")
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as TypeInfo

        return other.method == this.method && other.p == this.p && other.q == this.q
    }

    override fun hashCode(): Int = this.method.hashCode() + this.p.hashCode() + this.q.hashCode()
}