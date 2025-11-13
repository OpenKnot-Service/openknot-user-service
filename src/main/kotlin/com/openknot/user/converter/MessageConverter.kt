package com.openknot.user.converter

import com.openknot.user.exception.ErrorCode
import org.springframework.context.NoSuchMessageException
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale

class MessageConverter {
    private val messageSource: YamlMessageSource = YamlMessageSource()

    private val locale: Locale
        get() = LocaleContextHolder.getLocale()

    fun getMessage(
        code: String,
        vararg args: Any?,
    ): String {
        return try {
            messageSource.getMessage(code, args, locale)
        } catch (e: NoSuchMessageException) {
            messageSource.getMessage(ErrorCode.INVALID_ERROR_CODE.code, args, locale)
        }
    }
}