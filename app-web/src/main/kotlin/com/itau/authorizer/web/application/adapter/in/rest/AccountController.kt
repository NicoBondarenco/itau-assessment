package com.itau.authorizer.web.application.adapter.`in`.rest

import com.itau.authorizer.web.domain.usecase.DataUsecase
import com.itau.authorizer.web.domain.usecase.TransactionUsecase
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/accounts")
class AccountController(
    private val accountDataUsecase: DataUsecase
) {

    @PostMapping("/create-batch")
    suspend fun createBatch(
        @RequestParam(value = "quantity", required = false) quantity: Int = 1,
    ): ResponseEntity<Unit> = accountDataUsecase.generateData(
        quantity
    ).let {
        ok().build()
    }

}
