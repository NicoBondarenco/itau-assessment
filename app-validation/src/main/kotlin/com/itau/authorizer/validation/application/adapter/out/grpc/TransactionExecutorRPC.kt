package com.itau.authorizer.validation.application.adapter.out.grpc

import com.itau.authorizer.authorization.application.adapter.`in`.grpc.ExecuteTransactionGrpcKt.ExecuteTransactionCoroutineStub
import com.itau.authorizer.common.application.mapper.toTransactionExecutionRequest
import com.itau.authorizer.common.domain.model.entity.TransactionEntity
import com.itau.authorizer.validation.domain.port.out.TransactionExecutorOut
import org.springframework.stereotype.Component

@Component
class TransactionExecutorRPC(
    private val channel: ExecuteTransactionCoroutineStub
) : TransactionExecutorOut {

    override suspend fun executeTransaction(entity: TransactionEntity) {
        channel.executeAccountTransaction(entity.toTransactionExecutionRequest())
    }

}
