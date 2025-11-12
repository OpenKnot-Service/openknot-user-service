package com.openknot.user

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OpenknotUserServiceApplication

fun main(args: Array<String>) {
    runApplication<OpenknotUserServiceApplication>(*args)
}
