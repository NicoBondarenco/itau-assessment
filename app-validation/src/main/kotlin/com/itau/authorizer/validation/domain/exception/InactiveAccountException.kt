package com.itau.authorizer.validation.domain.exception

import java.util.UUID

class InactiveAccountException(
    accountId: UUID,
) : RuntimeException("Account $accountId is inactive")
