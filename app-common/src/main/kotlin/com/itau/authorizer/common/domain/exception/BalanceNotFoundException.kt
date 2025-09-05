package com.itau.authorizer.common.domain.exception

import java.util.UUID

class BalanceNotFoundException(accountId: UUID) : RuntimeException(
    "Account $accountId has no balance"
)
