package com.itau.authorizer.common.domain.model.entity

import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.UUID

data class BalanceEntity(
    val accountId: UUID,
    val amount: BigDecimal,
    val lastUpdate: ZonedDateTime,
)
