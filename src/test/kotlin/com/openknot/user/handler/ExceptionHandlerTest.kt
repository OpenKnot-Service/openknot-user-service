package com.openknot.user.handler

import com.openknot.user.exception.BusinessException
import com.openknot.user.exception.ErrorCode
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

/**
 * ExceptionHandler 통합 테스트
 *
 * 테스트 전략:
 * - ExceptionHandler의 예외 처리 로직을 직접 호출하여 검증
 * - 다양한 BusinessException 시나리오에 대한 응답 구조 검증
 * - HTTP 상태 코드와 에러 응답 body 확인
 * - 로깅이 올바르게 수행되는지 간접 검증 (예외 발생 시 로그 출력)
 */
@DisplayName("ExceptionHandler 통합 테스트")
class ExceptionHandlerTest {

    private lateinit var exceptionHandler: ExceptionHandler

    @BeforeEach
    fun setUp() {
        exceptionHandler = ExceptionHandler()
    }

    @Test
    @DisplayName("handleBusinessException - USER_NOT_FOUND 예외를 404 응답으로 변환한다")
    fun `given user not found exception, when handle, then should return 404 response`() = runTest {
        // given: USER_NOT_FOUND 예외
        val exception = BusinessException(ErrorCode.USER_NOT_FOUND)

        // when: handleBusinessException 메서드를 호출할 때
        val response = exceptionHandler.handleBusinessException(exception)

        // then: 404 NOT_FOUND 응답이 반환되어야 한다
        response.statusCode shouldBe HttpStatus.NOT_FOUND
        response.body shouldNotBe null
        response.body!!.code shouldBe "USER.001"
        response.body!!.message shouldBe "유저를 찾을 수 없습니다."
    }

    @Test
    @DisplayName("handleBusinessException - DUPLICATE_EMAIL 예외를 409 응답으로 변환한다")
    fun `given duplicate email exception, when handle, then should return 409 response`() = runTest {
        // given: DUPLICATE_EMAIL 예외
        val exception = BusinessException(ErrorCode.DUPLICATE_EMAIL)

        // when: handleBusinessException 메서드를 호출할 때
        val response = exceptionHandler.handleBusinessException(exception)

        // then: 409 CONFLICT 응답이 반환되어야 한다
        response.statusCode shouldBe HttpStatus.CONFLICT
        response.body shouldNotBe null
        response.body!!.code shouldBe "USER.002"
        response.body!!.message shouldBe "중복 이메일 입니다."
    }

    @Test
    @DisplayName("handleBusinessException - WRONG_PASSWORD 예외를 401 응답으로 변환한다")
    fun `given wrong password exception, when handle, then should return 401 response`() = runTest {
        // given: WRONG_PASSWORD 예외
        val exception = BusinessException(ErrorCode.WRONG_PASSWORD)

        // when: handleBusinessException 메서드를 호출할 때
        val response = exceptionHandler.handleBusinessException(exception)

        // then: 401 UNAUTHORIZED 응답이 반환되어야 한다
        response.statusCode shouldBe HttpStatus.UNAUTHORIZED
        response.body shouldNotBe null
        response.body!!.code shouldBe "USER.003"
        response.body!!.message shouldBe "틀린 비밀번호입니다."
    }

    @Test
    @DisplayName("handleBusinessException - INVALID_ERROR_CODE 예외를 400 응답으로 변환한다")
    fun `given invalid error code exception, when handle, then should return 400 response`() = runTest {
        // given: INVALID_ERROR_CODE 예외
        val exception = BusinessException(ErrorCode.INVALID_ERROR_CODE)

        // when: handleBusinessException 메서드를 호출할 때
        val response = exceptionHandler.handleBusinessException(exception)

        // then: 400 BAD_REQUEST 응답이 반환되어야 한다
        response.statusCode shouldBe HttpStatus.BAD_REQUEST
        response.body shouldNotBe null
        response.body!!.code shouldBe "SYSTEM.001"
        response.body!!.message shouldBe "정의되지 않은 에러코드입니다."
    }

    @Test
    @DisplayName("handleBusinessException - VALIDATION_FAIL 예외를 400 응답으로 변환한다")
    fun `given validation fail exception, when handle, then should return 400 response`() = runTest {
        // given: VALIDATION_FAIL 예외
        val exception = BusinessException(ErrorCode.VALIDATION_FAIL)

        // when: handleBusinessException 메서드를 호출할 때
        val response = exceptionHandler.handleBusinessException(exception)

        // then: 400 BAD_REQUEST 응답이 반환되어야 한다
        response.statusCode shouldBe HttpStatus.BAD_REQUEST
        response.body shouldNotBe null
        response.body!!.code shouldBe "VALID.001"
        response.body!!.message shouldBe "유효성 검사에 실패했습니다."
    }

    @Test
    @DisplayName("handleBusinessException - REQUIRED_PARAMETER 예외를 400 응답으로 변환한다")
    fun `given required parameter exception, when handle, then should return 400 response`() = runTest {
        // given: REQUIRED_PARAMETER 예외
        val exception = BusinessException(ErrorCode.REQUIRED_PARAMETER)

        // when: handleBusinessException 메서드를 호출할 때
        val response = exceptionHandler.handleBusinessException(exception)

        // then: 400 BAD_REQUEST 응답이 반환되어야 한다
        response.statusCode shouldBe HttpStatus.BAD_REQUEST
        response.body shouldNotBe null
        response.body!!.code shouldBe "VALID.002"
        response.body!!.message shouldBe "필수 파라미터가 없습니다."
    }

    @Test
    @DisplayName("handleBusinessException - REQUIRED_PARAMETER_EMPTY 예외를 400 응답으로 변환한다")
    fun `given required parameter empty exception, when handle, then should return 400 response`() = runTest {
        // given: REQUIRED_PARAMETER_EMPTY 예외
        val exception = BusinessException(ErrorCode.REQUIRED_PARAMETER_EMPTY)

        // when: handleBusinessException 메서드를 호출할 때
        val response = exceptionHandler.handleBusinessException(exception)

        // then: 400 BAD_REQUEST 응답이 반환되어야 한다
        response.statusCode shouldBe HttpStatus.BAD_REQUEST
        response.body shouldNotBe null
        response.body!!.code shouldBe "VALID.003"
        response.body!!.message shouldBe "필수 파라미터가 없거나 비어있습니다."
    }

    @Test
    @DisplayName("handleBusinessException - 에러 응답의 구조가 올바르다")
    fun `given business exception, when handle, then should return response with correct structure`() = runTest {
        // given: 임의의 BusinessException
        val exception = BusinessException(ErrorCode.USER_NOT_FOUND)

        // when: handleBusinessException 메서드를 호출할 때
        val response = exceptionHandler.handleBusinessException(exception)

        // then: 응답 body가 올바른 구조를 가져야 한다
        val body = response.body
        body shouldNotBe null
        body!!.code shouldNotBe null
        body.message shouldNotBe null
        body.code.isNotEmpty() shouldBe true
        body.message.isNotEmpty() shouldBe true
    }

    @Test
    @DisplayName("handleBusinessException - 동일한 예외를 여러 번 처리해도 일관된 응답을 반환한다")
    fun `given same exception, when handle multiple times, then should return consistent responses`() = runTest {
        // given: 동일한 BusinessException
        val exception = BusinessException(ErrorCode.USER_NOT_FOUND)

        // when: handleBusinessException을 여러 번 호출할 때
        val response1 = exceptionHandler.handleBusinessException(exception)
        val response2 = exceptionHandler.handleBusinessException(exception)

        // then: 두 응답이 동일해야 한다
        response1.statusCode shouldBe response2.statusCode
        response1.body!!.code shouldBe response2.body!!.code
        response1.body!!.message shouldBe response2.body!!.message
    }

    @Test
    @DisplayName("handleBusinessException - 모든 ErrorCode가 올바른 HTTP 상태 코드를 반환한다")
    fun `given all error codes, when handle, then should return correct http status codes`() = runTest {
        // given & when & then: 각 ErrorCode에 대한 HTTP 상태 코드 검증
        val testCases = mapOf(
            ErrorCode.USER_NOT_FOUND to HttpStatus.NOT_FOUND,
            ErrorCode.DUPLICATE_EMAIL to HttpStatus.CONFLICT,
            ErrorCode.WRONG_PASSWORD to HttpStatus.UNAUTHORIZED,
            ErrorCode.INVALID_ERROR_CODE to HttpStatus.BAD_REQUEST,
            ErrorCode.VALIDATION_FAIL to HttpStatus.BAD_REQUEST,
            ErrorCode.REQUIRED_PARAMETER to HttpStatus.BAD_REQUEST,
            ErrorCode.REQUIRED_PARAMETER_EMPTY to HttpStatus.BAD_REQUEST
        )

        testCases.forEach { (errorCode, expectedStatus) ->
            val exception = BusinessException(errorCode)
            val response = exceptionHandler.handleBusinessException(exception)
            response.statusCode shouldBe expectedStatus
        }
    }
}
