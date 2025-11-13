package com.openknot.user.converter

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.context.NoSuchMessageException
import java.util.Locale

@DisplayName("YamlMessageSource 단위 테스트")
class YamlMessageSourceTest {

    private lateinit var messageSource: YamlMessageSource

    @BeforeEach
    fun setUp() {
        messageSource = YamlMessageSource()
    }

    @Test
    @DisplayName("USER 카테고리 코드가 올바르게 로드된다")
    fun `given user code, when getMessage, then should return localized string`() {
        val message = messageSource.getMessage("USER.001", emptyArray(), Locale.KOREAN)

        message shouldBe "유저를 찾을 수 없습니다."
    }

    @Test
    @DisplayName("플레이스홀더가 있는 VALID 코드도 MessageFormat으로 치환된다")
    fun `given placeholder code, when getMessage with args, then should substitute`() {
        val message = messageSource.getMessage("VALID.004", arrayOf("username", 3, 20), Locale.KOREAN)

        message shouldBe "username 파라미터의 길이는 3 ~ 20 사이로 지정 가능합니다."
    }

    @Test
    @DisplayName("존재하지 않는 코드는 NoSuchMessageException을 발생시킨다")
    fun `given unknown code, when getMessage, then should throw NoSuchMessageException`() {
        shouldThrow<NoSuchMessageException> {
            messageSource.getMessage("UNKNOWN.999", emptyArray(), Locale.KOREAN)
        }
    }
}
