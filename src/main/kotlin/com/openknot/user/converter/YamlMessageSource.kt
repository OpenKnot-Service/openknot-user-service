package com.openknot.user.converter

import org.springframework.context.support.AbstractMessageSource
import org.springframework.core.io.ClassPathResource
import org.yaml.snakeyaml.Yaml
import java.text.MessageFormat
import java.util.Locale

class YamlMessageSource : AbstractMessageSource() {
    private val messages: Map<String, Any> = loadMessages()

    private fun loadMessages(): Map<String, Any> {
        val resource = ClassPathResource("messages/messages.yml")
        return runCatching {
            if (resource.exists() && resource.isReadable) {
                resource.inputStream.use {
                    @Suppress("UNCHECKED_CAST")
                    (Yaml().load(it) as? Map<String, Any>) ?: DEFAULT_MESSAGES
                }
            } else {
                DEFAULT_MESSAGES
            }
        }.getOrDefault(DEFAULT_MESSAGES)
    }

    override fun resolveCode(
        code: String,
        locale: Locale,
    ): MessageFormat? {
        val message = getMessageFromYaml(code, messages)
        return message?.let { MessageFormat(it, locale) }
    }

    private fun getMessageFromYaml(
        code: String,
        messages: Map<String, Any>,
    ): String? {
        val keys = code.split(".")
        var value: Any? = messages
        for (key in keys) {
            if (value is Map<*, *>) {
                value =
                    value.entries.find { (entryKey, _) ->
                        entryKey.toString() == key || entryKey.toString() == key.toIntOrNull()?.toString()
                    }?.value
            } else {
                return null
            }
        }
        return value as? String
    }

    companion object {
        private val DEFAULT_MESSAGES = mapOf(
            "USER" to mapOf(
                "001" to "사용자를 찾을 수 없음",
                "002" to "중복 이메일 입니다.",
                "003" to "틀린 비밀번호입니다.",
            ),
            "SYSTEM" to mapOf(
                "001" to "정의되지 않은 에러코드입니다.",
            ),
            "VALID" to mapOf(
                "001" to "유효성 검사에 실패했습니다.",
                "002" to "필수 파라미터가 없습니다.",
                "003" to "필수 파라미터가 없거나 비어있습니다.",
                "004" to "{0} 파라미터의 길이는 {1} ~ {2} 사이로 지정 가능합니다.",
                "005" to "{0} 파라미터의 값은 최소 {1} 이상 지정 가능합니다.",
                "006" to "{0} 파라미터의 값은 최대 {1} 까지 지정 가능합니다.",
                "007" to "{0} 파라미터의 범위는 {1} ~ {2} 사이로 지정 가능합니다.",
            ),
        )
    }
}
