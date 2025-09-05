package com.itau.authorizer.web.application.model.rest

data class AccountDataResponse(
    val account: AccountResponse,
    val balance: BalanceResponse,
    val transactions: List<TransactionResponse>,
)
