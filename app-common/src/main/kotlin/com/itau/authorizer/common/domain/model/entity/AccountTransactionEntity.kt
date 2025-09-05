package com.itau.authorizer.common.domain.model.entity

import com.itau.authorizer.common.domain.model.value.TransactionType
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.UUID

data class AccountTransactionEntity(
    val transactionId: UUID,
    val accountId: UUID,
    val amount: BigDecimal,
    val type: TransactionType,
    val timestamp: ZonedDateTime,
    val currentBalance: BigDecimal,
)
