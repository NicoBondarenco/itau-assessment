package com.itau.authorizer.validation.application.adapter.mock.grpc

import com.google.protobuf.Empty
import com.itau.authorizer.common.application.adapter.`in`.grpc.ExecuteTransactionGrpcKt.ExecuteTransactionCoroutineStub
import com.itau.authorizer.common.application.model.grpc.TransactionExecutionRequest
import com.itau.authorizer.common.util.extension.toUUID
import io.mockk.coEvery
import io.mockk.mockk
import java.util.UUID

val executedTransactionsMock = mutableMapOf<UUID, TransactionExecutionRequest>()

fun mockExecuteTransactionCoroutineStub() = mockk<ExecuteTransactionCoroutineStub>().apply {
    coEvery { executeAccountTransaction(any(), any()) } answers {
        val request = firstArg<TransactionExecutionRequest>()
        executedTransactionsMock[request.accountId.toUUID()] = request
        Empty.getDefaultInstance()
    }
}
