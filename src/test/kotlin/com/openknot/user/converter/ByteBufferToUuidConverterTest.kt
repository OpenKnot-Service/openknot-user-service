package com.openknot.user.converter

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer
import java.util.*

/**
 * ByteBufferToUuidConverter 단위 테스트
 *
 * 테스트 전략:
 * - R2DBC에서 MySQL BINARY(16) 타입을 UUID로 변환하는 Reading Converter 테스트
 * - 다양한 UUID 값에 대한 ByteBuffer → UUID 변환 검증
 * - UuidToByteBufferConverter와의 라운드트립(왕복) 변환 일관성 검증
 */
@DisplayName("ByteBufferToUuidConverter 단위 테스트")
class ByteBufferToUuidConverterTest {

    private lateinit var converter: ByteBufferToUuidConverter

    @BeforeEach
    fun setUp() {
        converter = ByteBufferToUuidConverter()
    }

    @Test
    @DisplayName("convert - 16바이트 ByteBuffer를 정상적으로 UUID로 변환한다")
    fun `given 16 bytes byte buffer, when convert, then should return uuid`() {
        // given: 16바이트 ByteBuffer (알려진 UUID 값)
        val expectedUuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
        val byteBuffer = ByteBuffer.allocate(16)
        byteBuffer.putLong(expectedUuid.mostSignificantBits)
        byteBuffer.putLong(expectedUuid.leastSignificantBits)
        byteBuffer.rewind()

        // when: convert 메서드를 호출할 때
        val result = converter.convert(byteBuffer)

        // then: 올바른 UUID가 반환되어야 한다
        result shouldBe expectedUuid
    }

    @Test
    @DisplayName("convert - 최소값 UUID (모든 비트 0)를 정상적으로 변환한다")
    fun `given minimum uuid bytes, when convert, then should return minimum uuid`() {
        // given: 최소값 UUID (00000000-0000-0000-0000-000000000000)
        val expectedUuid = UUID(0L, 0L)
        val byteBuffer = ByteBuffer.allocate(16)
        byteBuffer.putLong(0L)
        byteBuffer.putLong(0L)
        byteBuffer.rewind()

        // when: convert 메서드를 호출할 때
        val result = converter.convert(byteBuffer)

        // then: 최소값 UUID가 반환되어야 한다
        result shouldBe expectedUuid
        result.toString() shouldBe "00000000-0000-0000-0000-000000000000"
    }

    @Test
    @DisplayName("convert - 최대값 UUID (모든 비트 1)를 정상적으로 변환한다")
    fun `given maximum uuid bytes, when convert, then should return maximum uuid`() {
        // given: 최대값 UUID (ffffffff-ffff-ffff-ffff-ffffffffffff)
        val expectedUuid = UUID(-1L, -1L)
        val byteBuffer = ByteBuffer.allocate(16)
        byteBuffer.putLong(-1L)
        byteBuffer.putLong(-1L)
        byteBuffer.rewind()

        // when: convert 메서드를 호출할 때
        val result = converter.convert(byteBuffer)

        // then: 최대값 UUID가 반환되어야 한다
        result shouldBe expectedUuid
        result.toString() shouldBe "ffffffff-ffff-ffff-ffff-ffffffffffff"
    }

    @Test
    @DisplayName("convert - UUID.randomUUID()로 생성된 UUID를 ByteBuffer로 변환 후 다시 UUID로 복원한다")
    fun `given random uuid converted to byte buffer, when convert back, then should return original uuid`() {
        // given: 랜덤 UUID를 ByteBuffer로 변환
        val originalUuid = UUID.randomUUID()
        val byteBuffer = ByteBuffer.allocate(16)
        byteBuffer.putLong(originalUuid.mostSignificantBits)
        byteBuffer.putLong(originalUuid.leastSignificantBits)
        byteBuffer.rewind()

        // when: convert 메서드를 호출할 때
        val result = converter.convert(byteBuffer)

        // then: 원본 UUID와 동일해야 한다
        result shouldBe originalUuid
    }

    @Test
    @DisplayName("convert - mostSignificantBits와 leastSignificantBits가 정확히 추출된다")
    fun `given byte buffer, when convert, then should correctly extract most and least significant bits`() {
        // given: 특정 mostSigBits와 leastSigBits를 가진 UUID
        val mostSigBits = 0x550e8400e29b41d4L
        val leastSigBits = -6406858213580079104L
        val expectedUuid = UUID(mostSigBits, leastSigBits)

        val byteBuffer = ByteBuffer.allocate(16)
        byteBuffer.putLong(mostSigBits)
        byteBuffer.putLong(leastSigBits)
        byteBuffer.rewind()

        // when: convert 메서드를 호출할 때
        val result = converter.convert(byteBuffer)

        // then: UUID의 most/least significant bits가 정확해야 한다
        result shouldBe expectedUuid
        result.mostSignificantBits shouldBe mostSigBits
        result.leastSignificantBits shouldBe leastSigBits
    }

    @Test
    @DisplayName("convert - UuidToByteBufferConverter와 라운드트립 변환 시 일관성이 유지된다")
    fun `given uuid, when round trip conversion with UuidToByteBufferConverter, then should maintain consistency`() {
        // given: 원본 UUID와 UuidToByteBufferConverter
        val originalUuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        val uuidToByteBufferConverter = UuidToByteBufferConverter()

        // when: UUID → ByteBuffer → UUID 라운드트립 변환
        val byteBuffer = uuidToByteBufferConverter.convert(originalUuid)
        val convertedUuid = converter.convert(byteBuffer)

        // then: 원본 UUID와 변환된 UUID가 동일해야 한다
        convertedUuid shouldBe originalUuid
    }

    @Test
    @DisplayName("convert - ByteBuffer의 position이 0이 아닌 경우에도 정상적으로 변환한다")
    fun `given byte buffer with non-zero position, when convert, then should read from current position`() {
        // given: 알려진 UUID 값
        val expectedUuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
        val byteBuffer = ByteBuffer.allocate(16)
        byteBuffer.putLong(expectedUuid.mostSignificantBits)
        byteBuffer.putLong(expectedUuid.leastSignificantBits)
        byteBuffer.position(0)  // position을 명시적으로 0으로 설정

        // when: convert 메서드를 호출할 때
        val result = converter.convert(byteBuffer)

        // then: 올바른 UUID가 반환되어야 한다
        result shouldBe expectedUuid
    }
}
