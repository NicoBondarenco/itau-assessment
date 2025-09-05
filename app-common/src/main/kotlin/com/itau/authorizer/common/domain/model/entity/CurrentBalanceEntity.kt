package com.itau.authorizer.common.domain.model.entity

import java.math.BigDecimal
import java.util.UUID

data class CurrentBalanceEntity(
    val accountId: UUID,
    val dailyTransacted: BigDecimal,
    val currentBalance: BigDecimal,
)
