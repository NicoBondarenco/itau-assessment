package com.itau.authorizer.authorization.domain.model.mapper

import com.itau.authorizer.authorization.domain.model.entity.AccountTransactionEntity
import com.itau.authorizer.authorization.domain.model.entity.TransactionEntity
import java.math.BigDecimal

fun TransactionEntity.toAccountTransactionEntity(
    currentBalance: BigDecimal,
) = AccountTransactionEntity(
    transactionId = this.transactionId,
    accountId = this.accountId,
    amount = this.amount,
    type = this.type,
    timestamp = this.timestamp,
    currentBalance = currentBalance,
)
