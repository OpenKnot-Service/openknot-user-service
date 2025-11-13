package com.openknot.user.converter

import com.openknot.user.exception.BusinessException
import com.openknot.user.exception.ErrorCode
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.*

/**
 * StringUuidConverter 단위 테스트
 *
 * 테스트 전략:
 * - 외부 의존성이 없는 순수 Converter이므로 모킹 불필요
 * - 다양한 UUID 형식에 대한 변환 검증
 * - 잘못된 형식에 대한 예외 처리 검증
 * - Edge case와 경계값 테스트 포함
 */
@DisplayName("StringUuidConverter 단위 테스트")
class StringUuidConverterTest {

    private lateinit var converter: StringUuidConverter

    @BeforeEach
    fun setUp() {
        converter = StringUuidConverter()
    }

    @Test
    @DisplayName("convert - 표준 UUID 문자열을 정상적으로 UUID 객체로 변환한다")
    fun `given valid uuid string, when convert, then should return uuid object`() {
        // given: 표준 UUID 문자열
        val uuidString = "550e8400-e29b-41d4-a716-446655440000"

        // when: convert 메서드를 호출할 때
        val result = converter.convert(uuidString)

        // then: 올바른 UUID 객체가 반환되어야 한다
        result shouldBe UUID.fromString(uuidString)
        result.toString() shouldBe uuidString
    }

    @Test
    @DisplayName("convert - 대문자 UUID 문자열도 정상적으로 변환한다")
    fun `given uppercase uuid string, when convert, then should return uuid object`() {
        // given: 대문자 UUID 문자열
        val uuidString = "550E8400-E29B-41D4-A716-446655440000"

        // when: convert 메서드를 호출할 때
        val result = converter.convert(uuidString)

        // then: UUID 객체가 반환되어야 한다 (대소문자 구분 없음)
        result.toString() shouldBe uuidString.lowercase()
    }

    @Test
    @DisplayName("convert - 혼합 대소문자 UUID 문자열도 정상적으로 변환한다")
    fun `given mixed case uuid string, when convert, then should return uuid object`() {
        // given: 혼합 대소문자 UUID 문자열
        val uuidString = "550e8400-E29B-41d4-A716-446655440000"

        // when: convert 메서드를 호출할 때
        val result = converter.convert(uuidString)

        // then: UUID 객체가 반환되어야 한다
        result.toString() shouldBe uuidString.lowercase()
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "invalid-uuid",
        "123",
        "550e8400-e29b-41d4-a716",  // 짧은 UUID
        "550e8400-e29b-41d4-a716-446655440000-extra",  // 긴 UUID
        "550e8400e29b41d4a716446655440000",  // 하이픈 없음
        "",  // 빈 문자열
        " ",  // 공백
        "550e8400-e29b-41d4-a716-44665544000g",  // 잘못된 문자 포함
        "550e8400-e29b41d4-a716-446655440000"  // 하이픈 위치 오류
    ])
    @DisplayName("convert - 잘못된 형식의 UUID 문자열일 때 BusinessException(INVALID_ERROR_CODE)을 발생시킨다")
    fun `given invalid uuid string format, when convert, then should throw BusinessException with INVALID_ERROR_CODE`(invalidUuidString: String) {
        // when & then: convert 호출 시 BusinessException이 발생해야 한다
        val exception = shouldThrow<BusinessException> {
            converter.convert(invalidUuidString)
        }

        // then: 예외의 에러 코드가 INVALID_ERROR_CODE여야 한다
        exception.errorCode shouldBe ErrorCode.INVALID_ERROR_CODE
    }

    @Test
    @DisplayName("convert - null 문자열(특수 케이스)을 처리할 때 BusinessException을 발생시킨다")
    fun `given null string, when convert, then should throw BusinessException`() {
        // given: null 문자열 (Kotlin에서는 String?로 처리되지만, Java 호환성 테스트)
        val nullString = "null"

        // when & then: convert 호출 시 BusinessException이 발생해야 한다
        val exception = shouldThrow<BusinessException> {
            converter.convert(nullString)
        }

        // then: 예외의 에러 코드가 INVALID_ERROR_CODE여야 한다
        exception.errorCode shouldBe ErrorCode.INVALID_ERROR_CODE
    }

    @Test
    @DisplayName("convert - UUID.randomUUID()로 생성된 UUID 문자열을 정상적으로 변환한다")
    fun `given random uuid string, when convert, then should return equivalent uuid object`() {
        // given: UUID.randomUUID()로 생성된 UUID 문자열
        val originalUuid = UUID.randomUUID()
        val uuidString = originalUuid.toString()

        // when: convert 메서드를 호출할 때
        val result = converter.convert(uuidString)

        // then: 원본 UUID와 동일한 UUID 객체가 반환되어야 한다
        result shouldBe originalUuid
        result.toString() shouldBe uuidString
    }

    @Test
    @DisplayName("convert - 최소값 UUID를 정상적으로 변환한다")
    fun `given minimum uuid string, when convert, then should return uuid object`() {
        // given: 최소값 UUID (모든 비트가 0)
        val minUuidString = "00000000-0000-0000-0000-000000000000"

        // when: convert 메서드를 호출할 때
        val result = converter.convert(minUuidString)

        // then: UUID 객체가 반환되어야 한다
        result shouldBe UUID.fromString(minUuidString)
        result.toString() shouldBe minUuidString
    }

    @Test
    @DisplayName("convert - 최대값 UUID를 정상적으로 변환한다")
    fun `given maximum uuid string, when convert, then should return uuid object`() {
        // given: 최대값 UUID (모든 비트가 1)
        val maxUuidString = "ffffffff-ffff-ffff-ffff-ffffffffffff"

        // when: convert 메서드를 호출할 때
        val result = converter.convert(maxUuidString)

        // then: UUID 객체가 반환되어야 한다
        result shouldBe UUID.fromString(maxUuidString)
        result.toString() shouldBe maxUuidString
    }
}
