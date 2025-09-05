package com.itau.authorizer.web.application.mapper

import com.itau.authorizer.common.application.adapter.model.dynamodb.TransactionDynamoDB
import com.itau.authorizer.common.application.adapter.model.sqs.TransactionCommandSQS
import com.itau.authorizer.common.domain.model.entity.TransactionEntity
import java.math.BigDecimal

fun TransactionEntity.toTransactionCommand(
    invalidTransactionId: String? = null,
    invalidAccountId: String? = null,
    invalidAmount: BigDecimal? = null,
    invalidType: String? = null,
) = TransactionCommandSQS(
    transactionId = invalidTransactionId ?: this.transactionId.toString(),
    accountId = invalidAccountId ?: this.accountId.toString(),
    amount = invalidAmount ?: this.amount,
    type = invalidType ?: this.type.toString(),
    timestamp = this.timestamp,
)

fun TransactionEntity.toTransactionDynamoDB() = TransactionDynamoDB(
    transactionId = this.transactionId.toString(),
    accountId = this.accountId.toString(),
    amount = this.amount,
    type = this.type.toString(),
    timestamp = this.timestamp,
)
