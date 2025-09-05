package com.itau.authorizer.web

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.itau.authorizer"])
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
