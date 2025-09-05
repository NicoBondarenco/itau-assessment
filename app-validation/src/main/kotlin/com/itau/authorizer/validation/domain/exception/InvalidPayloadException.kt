package com.itau.authorizer.validation.domain.exception

class InvalidPayloadException(
    message: String?,
) : RuntimeException("Invalid transaction payload received - $message")
