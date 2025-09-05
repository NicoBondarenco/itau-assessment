package com.itau.authorizer.common.application.mapper

import com.itau.authorizer.common.application.model.dynamodb.BalanceDynamoDB
import com.itau.authorizer.common.application.model.dynamodb.TransactionDynamoDB
import com.itau.authorizer.common.application.model.grpc.TransactionExecutionRequest
import com.itau.authorizer.common.domain.exception.InvalidTransactionException
import com.itau.authorizer.common.domain.model.entity.AccountTransactionEntity
import com.itau.authorizer.common.domain.model.entity.TransactionEntity
import com.itau.authorizer.common.domain.model.value.TransactionType
import com.itau.authorizer.common.util.extension.toAmountFormat
import com.itau.authorizer.common.util.extension.toIsoFormat
import com.itau.authorizer.common.util.extension.toUUID
import com.itau.authorizer.common.util.extension.toZonedDateTime
import java.time.ZonedDateTime.now

fun TransactionExecutionRequest.toTransactionEntity() = try {
    TransactionEntity(
        transactionId = this.transactionId.toUUID(),
        accountId = this.accountId.toUUID(),
        amount = this.amount.toBigDecimal(),
        type = TransactionType.valueOf(this.type),
        timestamp = this.timestamp.toZonedDateTime(),
    )
} catch (e: Exception) {
    throw InvalidTransactionException(this.toString(), e)
}

fun TransactionDynamoDB.toTransactionEntity() = TransactionEntity(
    transactionId = this.transactionId,
    accountId = this.accountId,
    amount = this.amount,
    type = this.type,
    timestamp = this.timestamp,
)

fun TransactionEntity.toTransactionDynamoDB() = TransactionDynamoDB(
    transactionId = this.transactionId,
    accountId = this.accountId,
    amount = this.amount,
    type = this.type,
    timestamp = this.timestamp,
)

fun AccountTransactionEntity.toTransactionDynamoDB() = TransactionDynamoDB(
    transactionId = this.transactionId,
    accountId = this.accountId,
    amount = this.amount,
    type = this.type,
    timestamp = this.timestamp,
)

fun AccountTransactionEntity.toBalanceDynamoDB() = BalanceDynamoDB(
    accountId = this.accountId,
    amount = this.currentBalance,
    lastUpdate = now(),
)

fun TransactionEntity.toTransactionExecutionRequest(): TransactionExecutionRequest = TransactionExecutionRequest.newBuilder()
    .setTransactionId(this.transactionId.toString())
    .setAccountId(this.accountId.toString())
    .setAmount(this.amount.toAmountFormat())
    .setType(this.type.toString())
    .setTimestamp(this.timestamp.toIsoFormat())
    .build()
