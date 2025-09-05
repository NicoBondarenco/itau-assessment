package com.itau.authorizer.validation

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.itau.authorizer"])
class Application

fun main(args: Array<String>) {
    runApplication<com.itau.authorizer.validation.Application>(*args)
}
