package com.itau.authorizer.validation.domain.exception

import java.util.UUID

class InvalidAmountException(
    accountId: UUID,
    transactionId: UUID,
) : RuntimeException("Amount for transaction $transactionId on account $accountId is lower or equal to zero")
