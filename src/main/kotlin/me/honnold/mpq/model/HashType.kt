package me.honnold.mpq.model

enum class HashType(val offset: Int) {
    TABLE_OFFSET(0 shl 8),
    HASH_A(1 shl 8),
    HASH_B(2 shl 8),
    TABLE(3 shl 8)
}