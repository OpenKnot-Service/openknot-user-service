package com.openknot.user.converter

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import java.nio.ByteBuffer
import java.util.UUID

@ReadingConverter
class ByteBufferToUuidConverter : Converter<ByteBuffer, UUID> {
    override fun convert(source: ByteBuffer): UUID {
        val mostSigBits = source.getLong()
        val leastSigBits = source.getLong()
        return UUID(mostSigBits, leastSigBits)
    }
}