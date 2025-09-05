package com.itau.authorizer.web.application.adapter.`in`.rest

import com.itau.authorizer.web.application.mapper.toAccountDataResponse
import com.itau.authorizer.web.application.model.rest.AccountDataResponse
import com.itau.authorizer.web.domain.usecase.AccountDataUsecase
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/accounts")
class AccountController(
    private val accountDataUsecase: AccountDataUsecase
) {

    @PostMapping("/create-batch")
    suspend fun createBatch(
        @RequestParam(value = "quantity", required = false) quantity: Int = 1,
    ): ResponseEntity<Unit> = accountDataUsecase.generateData(
        quantity
    ).let {
        ok().build()
    }

    @GetMapping("/{id}")
    suspend fun one(
        @PathVariable id: UUID
    ): ResponseEntity<AccountDataResponse> = ok(accountDataUsecase.one(id).toAccountDataResponse())

    @GetMapping
    suspend fun all(): ResponseEntity<List<AccountDataResponse>> = ok(accountDataUsecase.all().map { it.toAccountDataResponse() })

}
