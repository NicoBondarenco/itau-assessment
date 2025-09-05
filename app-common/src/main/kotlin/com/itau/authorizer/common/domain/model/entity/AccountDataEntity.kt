package com.itau.authorizer.common.domain.model.entity

data class AccountDataEntity(
    val account: AccountEntity,
    val balance: BalanceEntity,
    val transactions: List<TransactionEntity>,
)
