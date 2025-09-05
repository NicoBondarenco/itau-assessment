package com.itau.authorizer.common.domain.exception

import java.util.UUID

class AccountNotFoundException(accountId: UUID) : RuntimeException(
    "Account not found with ID: $accountId"
)
