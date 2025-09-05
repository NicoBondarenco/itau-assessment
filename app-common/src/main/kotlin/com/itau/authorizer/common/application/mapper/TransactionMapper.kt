package com.itau.authorizer.common.application.mapper

import com.itau.authorizer.common.application.model.dynamodb.BalanceDynamoDB
import com.itau.authorizer.common.application.model.dynamodb.TransactionDynamoDB
import com.itau.authorizer.common.application.model.grpc.TransactionExecutionRequest
import com.itau.authorizer.common.application.model.sqs.TransactionCommandSQS
import com.itau.authorizer.common.domain.exception.InvalidTransactionException
import com.itau.authorizer.common.domain.model.entity.AccountTransactionEntity
import com.itau.authorizer.common.domain.model.entity.TransactionEntity
import com.itau.authorizer.common.domain.model.value.TransactionType
import com.itau.authorizer.common.util.extension.toUUID
import com.itau.authorizer.common.util.extension.toZonedDateTime
import java.math.BigDecimal
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

