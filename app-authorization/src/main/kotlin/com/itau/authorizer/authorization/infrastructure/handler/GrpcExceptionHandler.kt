package com.itau.authorizer.authorization.infrastructure.handler

import com.itau.authorizer.common.domain.exception.BalanceNotFoundException
import io.grpc.Status
import io.grpc.StatusException
import org.springframework.grpc.server.exception.GrpcExceptionHandler
import org.springframework.stereotype.Component

@Component
class GrpcExceptionHandler : GrpcExceptionHandler {


    override fun handleException(
        exception: Throwable
    ): StatusException = when (exception) {
        is BalanceNotFoundException -> Status.NOT_FOUND
        else -> Status.INTERNAL
    }.withDescription(exception.message)
        .withCause(exception)
        .asException()


}
