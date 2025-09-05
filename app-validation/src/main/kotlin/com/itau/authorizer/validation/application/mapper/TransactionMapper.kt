package com.itau.authorizer.validation.application.mapper

import com.itau.authorizer.common.domain.model.entity.TransactionEntity
import com.itau.authorizer.validation.application.model.sqs.TransactionCommandSQS

fun TransactionCommandSQS.toTransactionEntity(): TransactionEntity = TransactionEntity(
    transactionId = this.transactionId,
    accountId = this.accountId,
    amount = this.amount,
    type = this.type,
    timestamp = this.timestamp,
)
