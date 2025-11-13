package com.openknot.user.exception

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("BusinessException 단위 테스트")
class BusinessExceptionTest {

    @Test
    @DisplayName("에러 코드에 해당하는 메시지가 YAML 리소스에서 로딩된다")
    fun `given error code, when instantiate, then should expose localized message`() {
        // when
        val exception = BusinessException(ErrorCode.USER_NOT_FOUND)

        // then
        exception.errorCode shouldBe ErrorCode.USER_NOT_FOUND
        exception.message shouldBe "유저를 찾을 수 없습니다."
    }

    @Test
    @DisplayName("SYSTEM 카테고리 에러도 일관된 포맷으로 노출된다")
    fun `given system error code, when instantiate, then should reuse message source`() {
        // when
        val exception = BusinessException(ErrorCode.INVALID_ERROR_CODE)

        // then
        exception.errorCode shouldBe ErrorCode.INVALID_ERROR_CODE
        exception.message shouldBe "정의되지 않은 에러코드입니다."
    }

    @Test
    @DisplayName("검증 계열 에러는 HTTP 400 상태에 해당하는 코드 문자열을 노출한다")
    fun `given validation error, when instantiate, then should carry validation prefix`() {
        // when
        val exception = BusinessException(ErrorCode.VALIDATION_FAIL)

        // then
        exception.message shouldContain "유효성 검사"
        exception.errorCode shouldBe ErrorCode.VALIDATION_FAIL
    }
}
