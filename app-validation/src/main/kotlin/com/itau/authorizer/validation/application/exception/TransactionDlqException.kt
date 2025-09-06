package com.itau.authorizer.validation.application.exception

import java.lang.RuntimeException

class TransactionDlqException(
    cause: Throwable,
): RuntimeException("Error sending message to transaction dlq. Cause: ${cause.message}", cause)
