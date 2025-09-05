package com.itau.authorizer.validation.application.model.sqs

import com.itau.authorizer.common.domain.model.value.TransactionType
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.UUID

data class TransactionCommandSQS(
    val transactionId: UUID,
    val accountId: UUID,
    val amount: BigDecimal,
    val type: TransactionType,
    val timestamp: ZonedDateTime
)
