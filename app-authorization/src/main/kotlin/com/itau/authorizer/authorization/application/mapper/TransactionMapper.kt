package com.itau.authorizer.authorization.application.mapper

import com.itau.authorizer.authorization.application.model.kafka.TransactionExecutedKafka
import com.itau.authorizer.common.domain.model.entity.AccountTransactionEntity
import com.itau.authorizer.common.util.extension.toIsoFormat

fun AccountTransactionEntity.toTransactionExecutedKafka() = TransactionExecutedKafka.newBuilder()
    .setTransactionId(this.transactionId)
    .setAccountId(this.accountId)
    .setAmount(this.amount)
    .setType(this.type.toString())
    .setTimestamp(this.timestamp.toIsoFormat())
    .setCurrentBalance(this.currentBalance)
    .build()
