package com.openknot.user.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
) {
    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER.001"),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "USER.002"),
    WRONG_PASSWORD(HttpStatus.UNAUTHORIZED, "USER.003"),

    // Validation
    VALIDATION_FAIL(HttpStatus.BAD_REQUEST, "VALID.001"),
    REQUIRED_PARAMETER(HttpStatus.BAD_REQUEST, "VALID.002"),
    REQUIRED_PARAMETER_EMPTY(HttpStatus.BAD_REQUEST, "VALID.003"),

    // System
    INVALID_ERROR_CODE(HttpStatus.BAD_REQUEST, "SYSTEM.001"),
}
