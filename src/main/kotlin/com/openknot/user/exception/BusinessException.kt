package com.openknot.user.exception

import com.openknot.user.converter.MessageConverter

class BusinessException(
    val errorCode: ErrorCode,
    vararg args: Any?,
    messageConverter: MessageConverter = MessageConverter(),
) : RuntimeException(messageConverter.getMessage(errorCode.code, *args))