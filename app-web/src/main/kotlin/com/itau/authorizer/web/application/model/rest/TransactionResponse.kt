package com.itau.authorizer.web.application.model.rest

import com.itau.authorizer.common.domain.model.value.TransactionType
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.UUID

data class TransactionResponse(
    val transactionId: UUID,
    val accountId: UUID,
    val amount: BigDecimal,
    val type: TransactionType,
    val timestamp: ZonedDateTime,
)
