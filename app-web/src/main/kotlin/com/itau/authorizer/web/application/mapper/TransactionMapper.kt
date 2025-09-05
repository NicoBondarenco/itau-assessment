package com.itau.authorizer.web.application.mapper

import com.itau.authorizer.common.domain.model.entity.TransactionEntity
import com.itau.authorizer.web.application.model.sqs.TransactionCommandSQS
import java.math.BigDecimal

fun TransactionEntity.toTransactionCommandSQS(
    invalidTransactionId: String?,
    invalidAccountId: String?,
    invalidAmount: BigDecimal?,
    invalidType: String?,
): TransactionCommandSQS = TransactionCommandSQS(
    transactionId = invalidTransactionId ?: this.transactionId.toString(),
    accountId = invalidAccountId ?: this.accountId.toString(),
    amount = invalidAmount ?: this.amount,
    type = invalidType ?: this.type.toString(),
    timestamp = this.timestamp,
)
