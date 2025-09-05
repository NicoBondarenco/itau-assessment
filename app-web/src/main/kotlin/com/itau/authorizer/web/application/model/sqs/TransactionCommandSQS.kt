package com.itau.authorizer.web.application.model.sqs

import java.math.BigDecimal
import java.time.ZonedDateTime

data class TransactionCommandSQS(
    val transactionId: String,
    val accountId: String,
    val amount: BigDecimal,
    val type: String,
    val timestamp: ZonedDateTime
)
