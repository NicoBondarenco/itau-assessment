package com.itau.authorizer.web.application.model.rest

import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.UUID

data class AccountResponse(
    val accountId: UUID,
    val createdAt: ZonedDateTime,
    val isActive: Boolean,
    val dailyLimit: BigDecimal,
)
