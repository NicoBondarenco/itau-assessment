package com.itau.authorizer.web.application.model.rest

import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.UUID

data class BalanceResponse(
    val accountId: UUID,
    val amount: BigDecimal,
    val lastUpdate: ZonedDateTime,
)
