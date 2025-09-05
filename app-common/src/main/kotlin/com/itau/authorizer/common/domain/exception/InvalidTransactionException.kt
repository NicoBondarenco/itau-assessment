package com.itau.authorizer.common.domain.exception

class InvalidTransactionException(
    payload: String,
    cause: Throwable? = null,
) : RuntimeException(
    "Invalid transaction received: $payload",
    cause
)
