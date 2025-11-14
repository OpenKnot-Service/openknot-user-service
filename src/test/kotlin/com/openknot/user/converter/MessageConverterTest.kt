package com.openknot.user.converter

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.context.i18n.LocaleContextHolder
import java.text.NumberFormat
import java.util.*

/**
 * MessageConverter 단위 테스트
 *
 * 테스트 전략:
 * - YAML 기반 메시지 소스에서 메시지 로딩 검증
 * - 파라미터 치환 기능 검증
 * - 존재하지 않는 코드 처리 검증 (fallback to INVALID_ERROR_CODE)
 * - 다양한 에러 코드 카테고리 테스트
 */
@DisplayName("MessageConverter 단위 테스트")
class MessageConverterTest {

    private lateinit var messageConverter: MessageConverter

    @BeforeEach
    fun setUp() {
        messageConverter = MessageConverter()
        // 기본 로케일을 한국어로 설정
        LocaleContextHolder.setLocale(Locale.KOREAN)
    }

    @AfterEach
    fun tearDown() {
        LocaleContextHolder.resetLocaleContext()
    }

    @Test
    @DisplayName("getMessage - USER 카테고리의 메시지를 정상적으로 조회한다")
    fun `given user error code, when getMessage, then should return user error message`() {
        // given: USER 카테고리의 에러 코드
        val code = "USER.001"

        // when: getMessage 메서드를 호출할 때
        val result = messageConverter.getMessage(code)

        // then: 올바른 메시지가 반환되어야 한다
        result shouldBe "사용자를 찾을 수 없음"
    }

    @Test
    @DisplayName("getMessage - SYSTEM 카테고리의 메시지를 정상적으로 조회한다")
    fun `given system error code, when getMessage, then should return system error message`() {
        // given: SYSTEM 카테고리의 에러 코드
        val code = "SYSTEM.001"

        // when: getMessage 메서드를 호출할 때
        val result = messageConverter.getMessage(code)

        // then: 올바른 메시지가 반환되어야 한다
        result shouldBe "정의되지 않은 에러코드입니다."
    }

    @Test
    @DisplayName("getMessage - VALID 카테고리의 메시지를 정상적으로 조회한다")
    fun `given valid error code, when getMessage, then should return validation error message`() {
        // given: VALID 카테고리의 에러 코드
        val code = "VALID.001"

        // when: getMessage 메서드를 호출할 때
        val result = messageConverter.getMessage(code)

        // then: 올바른 메시지가 반환되어야 한다
        result shouldBe "유효성 검사에 실패했습니다."
    }

    @Test
    @DisplayName("getMessage - 파라미터 치환이 정상적으로 작동한다 (단일 파라미터)")
    fun `given code with placeholder and single argument, when getMessage, then should substitute parameter`() {
        // given: 파라미터 플레이스홀더가 있는 에러 코드와 치환할 값
        val code = "VALID.005"
        val parameterName = "age"
        val minValue = 18

        // when: getMessage 메서드를 파라미터와 함께 호출할 때
        val result = messageConverter.getMessage(code, parameterName, minValue)

        // then: 파라미터가 치환된 메시지가 반환되어야 한다
        result shouldBe "age 파라미터의 값은 최소 18 이상 지정 가능합니다."
    }

    @Test
    @DisplayName("getMessage - 파라미터 치환이 정상적으로 작동한다 (복수 파라미터)")
    fun `given code with multiple placeholders and arguments, when getMessage, then should substitute all parameters`() {
        // given: 여러 파라미터 플레이스홀더가 있는 에러 코드와 치환할 값들
        val code = "VALID.004"
        val parameterName = "username"
        val minLength = 3
        val maxLength = 20

        // when: getMessage 메서드를 여러 파라미터와 함께 호출할 때
        val result = messageConverter.getMessage(code, parameterName, minLength, maxLength)

        // then: 모든 파라미터가 치환된 메시지가 반환되어야 한다
        result shouldBe "username 파라미터의 길이는 3 ~ 20 사이로 지정 가능합니다."
    }

    @Test
    @DisplayName("getMessage - 파라미터 치환이 정상적으로 작동한다 (범위 파라미터)")
    fun `given code with range placeholders and arguments, when getMessage, then should substitute range parameters`() {
        // given: 범위 파라미터 플레이스홀더가 있는 에러 코드와 치환할 값들
        val code = "VALID.007"
        val parameterName = "score"
        val minValue = 0
        val maxValue = 100

        // when: getMessage 메서드를 범위 파라미터와 함께 호출할 때
        val result = messageConverter.getMessage(code, parameterName, minValue, maxValue)

        // then: 범위 파라미터가 치환된 메시지가 반환되어야 한다
        result shouldBe "score 파라미터의 범위는 0 ~ 100 사이로 지정 가능합니다."
    }

    @Test
    @DisplayName("getMessage - 존재하지 않는 에러 코드일 때 INVALID_ERROR_CODE 메시지를 반환한다")
    fun `given non-existing error code, when getMessage, then should return invalid error code message`() {
        // given: 존재하지 않는 에러 코드
        val nonExistingCode = "UNKNOWN.999"

        // when: getMessage 메서드를 호출할 때
        val result = messageConverter.getMessage(nonExistingCode)

        // then: INVALID_ERROR_CODE 메시지가 반환되어야 한다
        result shouldBe "정의되지 않은 에러코드입니다."
    }

    @Test
    @DisplayName("getMessage - 잘못된 형식의 에러 코드일 때 INVALID_ERROR_CODE 메시지를 반환한다")
    fun `given malformed error code, when getMessage, then should return invalid error code message`() {
        // given: 잘못된 형식의 에러 코드
        val malformedCode = "INVALID_FORMAT"

        // when: getMessage 메서드를 호출할 때
        val result = messageConverter.getMessage(malformedCode)

        // then: INVALID_ERROR_CODE 메시지가 반환되어야 한다
        result shouldBe "정의되지 않은 에러코드입니다."
    }

    @Test
    @DisplayName("getMessage - USER.002 (중복 이메일) 메시지를 정상적으로 조회한다")
    fun `given duplicate email error code, when getMessage, then should return duplicate email message`() {
        // given: USER.002 에러 코드
        val code = "USER.002"

        // when: getMessage 메서드를 호출할 때
        val result = messageConverter.getMessage(code)

        // then: 중복 이메일 메시지가 반환되어야 한다
        result shouldBe "중복 이메일 입니다."
    }

    @Test
    @DisplayName("getMessage - USER.003 (틀린 비밀번호) 메시지를 정상적으로 조회한다")
    fun `given wrong password error code, when getMessage, then should return wrong password message`() {
        // given: USER.003 에러 코드
        val code = "USER.003"

        // when: getMessage 메서드를 호출할 때
        val result = messageConverter.getMessage(code)

        // then: 틀린 비밀번호 메시지가 반환되어야 한다
        result shouldBe "틀린 비밀번호입니다."
    }

    @Test
    @DisplayName("getMessage - VALID.002 (필수 파라미터) 메시지를 정상적으로 조회한다")
    fun `given required parameter error code, when getMessage, then should return required parameter message`() {
        // given: VALID.002 에러 코드
        val code = "VALID.002"

        // when: getMessage 메서드를 호출할 때
        val result = messageConverter.getMessage(code)

        // then: 필수 파라미터 메시지가 반환되어야 한다
        result shouldBe "필수 파라미터가 없습니다."
    }

    @Test
    @DisplayName("getMessage - VALID.006 (최대값) 메시지에 파라미터를 정상적으로 치환한다")
    fun `given max value error code with parameters, when getMessage, then should substitute parameters correctly`() {
        // given: VALID.006 에러 코드와 파라미터
        val code = "VALID.006"
        val parameterName = "fileSize"
        val maxValue = 10485760  // 10MB
        val formattedMaxValue = NumberFormat.getInstance(Locale.KOREAN).format(maxValue)

        // when: getMessage 메서드를 호출할 때
        val result = messageConverter.getMessage(code, parameterName, maxValue)

        // then: 파라미터가 치환된 메시지가 반환되어야 한다
        result shouldContain parameterName
        result shouldContain formattedMaxValue
    }
}
