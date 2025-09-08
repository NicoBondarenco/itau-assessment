package com.itau.authorizer.authorization.application.adapter.`in`.grpc

import com.itau.authorizer.common.application.adapter.out.grpc.RetrieveCurrentBalanceGrpcKt.RetrieveCurrentBalanceCoroutineStub
import com.itau.authorizer.common.application.mapper.toCurrentBalanceEntity
import com.itau.authorizer.common.application.mapper.toCurrentBalanceRequest
import com.itau.authorizer.common.domain.model.entity.CurrentBalanceEntity
import java.util.UUID
import org.springframework.stereotype.Component

@Component
class TestCurrentBalanceRetrieverRPC(
    private val channel: RetrieveCurrentBalanceCoroutineStub
) {

    suspend fun accountCurrentBalance(
        accountId: UUID
    ): CurrentBalanceEntity = channel
        .accountCurrentBalance(accountId.toCurrentBalanceRequest())
        .toCurrentBalanceEntity()

}
