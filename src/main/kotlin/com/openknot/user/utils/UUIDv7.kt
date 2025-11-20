package com.openknot.user.utils

import java.security.SecureRandom
import java.util.UUID
import kotlin.also

object UUIDv7 {
    private val numberGenerator = SecureRandom()

    fun randomUUID(): UUID {
        val value = randomBytes()
        val high = value.toLong(0)
        val low = value.toLong(8)
        return UUID(high, low)
    }

    private fun randomBytes(): ByteArray {
        val value = ByteArray(16).also { numberGenerator.nextBytes(it) }

        val timestamp = System.currentTimeMillis()
        value[0] = ((timestamp shr 40) and 0xFF).toByte()
        value[1] = ((timestamp shr 32) and 0xFF).toByte()
        value[2] = ((timestamp shr 24) and 0xFF).toByte()
        value[3] = ((timestamp shr 16) and 0xFF).toByte()
        value[4] = ((timestamp shr 8) and 0xFF).toByte()
        value[5] = (timestamp and 0xFF).toByte()

        value[6] = ((value[6].toInt() and 0x0F) or 0x70).toByte()
        value[8] = ((value[8].toInt() and 0x3F) or 0x80).toByte()

        return value
    }

    private fun ByteArray.toLong(offset: Int = 0): Long {
        return ((this[offset].toLong() and 0xFF) shl 56) or
                ((this[offset + 1].toLong() and 0xFF) shl 48) or
                ((this[offset + 2].toLong() and 0xFF) shl 40) or
                ((this[offset + 3].toLong() and 0xFF) shl 32) or
                ((this[offset + 4].toLong() and 0xFF) shl 24) or
                ((this[offset + 5].toLong() and 0xFF) shl 16) or
                ((this[offset + 6].toLong() and 0xFF) shl 8) or
                (this[offset + 7].toLong() and 0xFF)
    }
}