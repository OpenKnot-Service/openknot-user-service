package com.openknot.user.converter

import org.springframework.context.support.AbstractMessageSource
import org.springframework.core.io.ClassPathResource
import org.yaml.snakeyaml.Yaml
import java.text.MessageFormat
import java.util.Locale

class YamlMessageSource : AbstractMessageSource() {
    private var messages: Map<String, Any> = emptyMap()

    init {
        val resourceName = "messages.yml"
        val resource = ClassPathResource("messages/$resourceName")
        messages = Yaml().load(resource.inputStream)
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
}