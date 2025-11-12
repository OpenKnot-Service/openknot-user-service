package com.openknot.user.converter

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.WritingConverter
import java.nio.ByteBuffer
import java.util.UUID

@WritingConverter
class UuidToByteBufferConverter : Converter<UUID, ByteBuffer> {
    override fun convert(source: UUID): ByteBuffer {
        val buffer = ByteBuffer.wrap(ByteArray(16))
        buffer.putLong(source.mostSignificantBits)
        buffer.putLong(source.leastSignificantBits)
        return buffer.rewind() as ByteBuffer
    }
}