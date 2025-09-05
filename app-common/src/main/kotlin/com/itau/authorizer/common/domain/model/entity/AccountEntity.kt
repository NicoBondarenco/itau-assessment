package com.itau.authorizer.common.domain.model.entity

import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.UUID

data class AccountEntity(
    val accountId: UUID,
    val createdAt: ZonedDateTime,
    val isActive: Boolean,
    val dailyLimit: BigDecimal,
)
