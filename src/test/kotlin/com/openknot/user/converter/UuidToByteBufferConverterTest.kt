package com.openknot.user.converter

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer
import java.util.*

/**
 * UuidToByteBufferConverter 단위 테스트
 *
 * 테스트 전략:
 * - R2DBC에서 UUID를 MySQL BINARY(16) 타입으로 변환하는 Writing Converter 테스트
 * - 다양한 UUID 값에 대한 UUID → ByteBuffer 변환 검증
 * - ByteBuffer의 크기와 내용이 정확한지 검증
 * - ByteBufferToUuidConverter와의 라운드트립(왕복) 변환 일관성 검증
 */
@DisplayName("UuidToByteBufferConverter 단위 테스트")
class UuidToByteBufferConverterTest {

    private lateinit var converter: UuidToByteBufferConverter

    @BeforeEach
    fun setUp() {
        converter = UuidToByteBufferConverter()
    }

    @Test
    @DisplayName("convert - UUID를 정상적으로 16바이트 ByteBuffer로 변환한다")
    fun `given uuid, when convert, then should return 16 bytes byte buffer`() {
        // given: 알려진 UUID
        val uuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")

        // when: convert 메서드를 호출할 때
        val result = converter.convert(uuid)

        // then: 16바이트 ByteBuffer가 반환되어야 한다
        result.remaining() shouldBe 16
    }

    @Test
    @DisplayName("convert - UUID의 mostSignificantBits와 leastSignificantBits가 정확히 저장된다")
    fun `given uuid, when convert, then should correctly store most and least significant bits`() {
        // given: 알려진 UUID
        val uuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
        val expectedMostSigBits = uuid.mostSignificantBits
        val expectedLeastSigBits = uuid.leastSignificantBits

        // when: convert 메서드를 호출할 때
        val result = converter.convert(uuid)

        // then: ByteBuffer에서 읽은 값이 UUID의 비트와 일치해야 한다
        val mostSigBits = result.getLong()
        val leastSigBits = result.getLong()

        mostSigBits shouldBe expectedMostSigBits
        leastSigBits shouldBe expectedLeastSigBits
    }

    @Test
    @DisplayName("convert - 최소값 UUID (모든 비트 0)를 정상적으로 변환한다")
    fun `given minimum uuid, when convert, then should return byte buffer with all zeros`() {
        // given: 최소값 UUID
        val uuid = UUID(0L, 0L)

        // when: convert 메서드를 호출할 때
        val result = converter.convert(uuid)

        // then: 모든 바이트가 0이어야 한다
        val mostSigBits = result.getLong()
        val leastSigBits = result.getLong()

        mostSigBits shouldBe 0L
        leastSigBits shouldBe 0L
    }

    @Test
    @DisplayName("convert - 최대값 UUID (모든 비트 1)를 정상적으로 변환한다")
    fun `given maximum uuid, when convert, then should return byte buffer with all ones`() {
        // given: 최대값 UUID
        val uuid = UUID(-1L, -1L)

        // when: convert 메서드를 호출할 때
        val result = converter.convert(uuid)

        // then: 모든 바이트가 1이어야 한다 (Long으로 -1)
        val mostSigBits = result.getLong()
        val leastSigBits = result.getLong()

        mostSigBits shouldBe -1L
        leastSigBits shouldBe -1L
    }

    @Test
    @DisplayName("convert - UUID.randomUUID()로 생성된 UUID를 정상적으로 변환한다")
    fun `given random uuid, when convert, then should return valid byte buffer`() {
        // given: 랜덤 UUID
        val uuid = UUID.randomUUID()

        // when: convert 메서드를 호출할 때
        val result = converter.convert(uuid)

        // then: 16바이트 ByteBuffer가 반환되고 UUID의 비트와 일치해야 한다
        result.remaining() shouldBe 16

        val mostSigBits = result.getLong()
        val leastSigBits = result.getLong()

        mostSigBits shouldBe uuid.mostSignificantBits
        leastSigBits shouldBe uuid.leastSignificantBits
    }

    @Test
    @DisplayName("convert - ByteBufferToUuidConverter와 라운드트립 변환 시 일관성이 유지된다")
    fun `given uuid, when round trip conversion with ByteBufferToUuidConverter, then should maintain consistency`() {
        // given: 원본 UUID와 ByteBufferToUuidConverter
        val originalUuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        val byteBufferToUuidConverter = ByteBufferToUuidConverter()

        // when: UUID → ByteBuffer → UUID 라운드트립 변환
        val byteBuffer = converter.convert(originalUuid)
        val convertedUuid = byteBufferToUuidConverter.convert(byteBuffer)

        // then: 원본 UUID와 변환된 UUID가 동일해야 한다
        convertedUuid shouldBe originalUuid
    }

    @Test
    @DisplayName("convert - ByteBuffer의 position이 0으로 리셋된다 (rewind 확인)")
    fun `given uuid, when convert, then should return byte buffer with position at zero`() {
        // given: 알려진 UUID
        val uuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")

        // when: convert 메서드를 호출할 때
        val result = converter.convert(uuid)

        // then: ByteBuffer의 position이 0이어야 한다 (rewind 호출 확인)
        result.position() shouldBe 0
        result.remaining() shouldBe 16
    }

    @Test
    @DisplayName("convert - 동일한 UUID를 여러 번 변환해도 일관된 결과를 반환한다")
    fun `given same uuid, when convert multiple times, then should return consistent results`() {
        // given: 동일한 UUID
        val uuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")

        // when: 여러 번 변환
        val result1 = converter.convert(uuid)
        val result2 = converter.convert(uuid)

        // then: 두 결과가 동일해야 한다
        val bytes1 = ByteArray(16)
        val bytes2 = ByteArray(16)

        result1.get(bytes1)
        result2.get(bytes2)

        bytes1 shouldBe bytes2
    }

    @Test
    @DisplayName("convert - 특정 mostSigBits와 leastSigBits를 가진 UUID를 정확히 변환한다")
    fun `given uuid with specific bits, when convert, then should correctly encode bits`() {
        // given: 특정 비트 값을 가진 UUID
        val mostSigBits = 0x123456789abcdef0L
        val leastSigBits = -81985529216486896L
        val uuid = UUID(mostSigBits, leastSigBits)

        // when: convert 메서드를 호출할 때
        val result = converter.convert(uuid)

        // then: ByteBuffer에서 읽은 값이 정확해야 한다
        val readMostSigBits = result.getLong()
        val readLeastSigBits = result.getLong()

        readMostSigBits shouldBe mostSigBits
        readLeastSigBits shouldBe leastSigBits
    }
}
