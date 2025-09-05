package com.itau.authorizer.validation.domain.exception

import java.util.UUID

class InsufficientFundsException(
    accountId: UUID,
    transactionId: UUID,
) : RuntimeException("Insufficient funds for transaction $transactionId on account $accountId")
