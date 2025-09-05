package com.itau.authorizer.validation.domain.exception

import java.util.UUID

class LimitReachedException(
    accountId: UUID,
    transactionId: UUID,
) : RuntimeException("Daily limit reached for transaction $transactionId on account $accountId")
