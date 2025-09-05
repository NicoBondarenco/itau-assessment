package com.itau.authorizer.web.application.adapter.`in`.rest

import com.itau.authorizer.web.domain.usecase.TransactionUsecase
import java.math.BigDecimal
import java.math.BigDecimal.ZERO
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/transactions")
class TransactionController(
    private val transactionUsecase: TransactionUsecase
) {

    @PostMapping("/produce-commands")
    suspend fun produceCommands(
        @RequestParam(value = "quantity", required = false) quantity: Int = 1,
    ): ResponseEntity<Unit> = transactionUsecase.produceCommands(
        quantity
    ).let {
        ok().build()
    }

    @PostMapping("/produce-with-amount")
    suspend fun produceWithAmount(
        @RequestParam(value = "amount", required = false) amount: BigDecimal = ZERO,
    ): ResponseEntity<Unit> = transactionUsecase.produceCommandWithAmount(
        amount
    ).let {
        ok().build()
    }

    @PostMapping("/produce-error-commands")
    suspend fun produceErrorCommands(
        @RequestParam(value = "quantity", required = false) quantity: Int = 1,
    ): ResponseEntity<Unit> = transactionUsecase.produceErrorCommands(
        quantity
    ).let {
        ok().build()
    }

}
