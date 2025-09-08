package com.itau.authorizer.authorization.application.adapter.out.grpc

import com.itau.authorizer.common.application.adapter.`in`.grpc.ExecuteTransactionGrpcKt.ExecuteTransactionCoroutineStub
import com.itau.authorizer.common.application.mapper.toTransactionExecutionRequest
import com.itau.authorizer.common.domain.model.entity.TransactionEntity
import org.springframework.stereotype.Component

@Component
class TestTransactionExecutorRPC(
    private val channel: ExecuteTransactionCoroutineStub
) {

    suspend fun executeTransaction(entity: TransactionEntity) {
        channel.executeAccountTransaction(entity.toTransactionExecutionRequest())
    }

}
