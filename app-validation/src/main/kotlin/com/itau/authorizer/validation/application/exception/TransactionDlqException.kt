package com.itau.authorizer.validation.application.exception

class TransactionDlqException(
    cause: Throwable,
) : RuntimeException("Error sending message to transaction dlq. Cause: ${cause.message}", cause)
