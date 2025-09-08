package com.itau.authorizer.authorization.application.adapter.`in`.grpc

import com.google.protobuf.Empty
import com.itau.authorizer.authorization.domain.usecase.ExecuteTransactionUsecase
import com.itau.authorizer.common.application.adapter.`in`.grpc.ExecuteTransactionGrpcKt.ExecuteTransactionCoroutineImplBase
import com.itau.authorizer.common.application.mapper.toTransactionEntity
import com.itau.authorizer.common.application.model.grpc.TransactionExecutionRequest
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.grpc.server.service.GrpcService

@GrpcService
class ExecuteTransactionRPC(
    private val executeTransactionUseCase: ExecuteTransactionUsecase
) : ExecuteTransactionCoroutineImplBase() {

    private val logger = KotlinLogging.logger { }

    override suspend fun executeAccountTransaction(
        request: TransactionExecutionRequest
    ): Empty = try {
        executeTransactionUseCase
            .executeTransaction(request.toTransactionEntity())
            .let { Empty.getDefaultInstance() }
    } catch (e: Exception) {
        logger.error(e) { "Error while executing transaction: ${e.message}" }
        throw e
    }

}
