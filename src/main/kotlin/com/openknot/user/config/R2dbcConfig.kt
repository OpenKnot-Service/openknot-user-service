package com.openknot.user.config

import com.openknot.user.converter.ByteBufferToUuidConverter
import com.openknot.user.converter.UuidToByteBufferConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import org.springframework.data.r2dbc.dialect.MySqlDialect

@Configuration
class R2dbcConfig {

    @Bean
    fun r2dbcCustomConversions(): R2dbcCustomConversions {
        val converters = listOf(
            ByteBufferToUuidConverter(),
            UuidToByteBufferConverter(),
        )

        return R2dbcCustomConversions.of(MySqlDialect.INSTANCE, converters)
    }
}