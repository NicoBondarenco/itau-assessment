package com.itau.authorizer.validation.application.adapter.`in`.grpc

import com.itau.authorizer.authorization.application.adapter.out.grpc.RetrieveCurrentBalanceGrpcKt.RetrieveCurrentBalanceCoroutineStub
import com.itau.authorizer.common.application.mapper.toCurrentBalanceEntity
import com.itau.authorizer.common.application.mapper.toCurrentBalanceRequest
import com.itau.authorizer.common.domain.model.entity.CurrentBalanceEntity
import com.itau.authorizer.validation.domain.port.`in`.CurrentBalanceRetrieverIn
import java.util.UUID
import org.springframework.stereotype.Component

@Component
class CurrentBalanceRetrieverRPC(
    private val channel: RetrieveCurrentBalanceCoroutineStub
) : CurrentBalanceRetrieverIn {

    override suspend fun accountCurrentBalance(
        accountId: UUID
    ): CurrentBalanceEntity = channel.accountCurrentBalance(accountId.toCurrentBalanceRequest())
        .toCurrentBalanceEntity()

}
