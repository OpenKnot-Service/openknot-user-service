package com.openknot.user

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

@SpringBootTest
class OpenknotUserServiceApplicationTests {

    companion object {
        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.r2dbc.url") { "r2dbc:mysql://localhost:3306/test" }
            registry.add("spring.r2dbc.username") { "test" }
            registry.add("spring.r2dbc.password") { "test" }
        }
    }

    @Test
    fun contextLoads() {
    }

}
