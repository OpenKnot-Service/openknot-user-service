package com.openknot.user.handler

import com.openknot.user.dto.ErrorResponse
import com.openknot.user.exception.BusinessException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler {
    private val logger = KotlinLogging.logger { }

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(be: BusinessException): ResponseEntity<ErrorResponse> {
        val errorCode = be.errorCode
        val code = errorCode.code
        val body = ErrorResponse(
            code = code,
            message = be.message!!,
        )
        logger.error { "BusinessException: $code, ${be.message}" }
        return ResponseEntity.status(errorCode.status).body(body)
    }
}