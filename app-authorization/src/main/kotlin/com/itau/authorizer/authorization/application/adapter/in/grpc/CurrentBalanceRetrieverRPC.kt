package com.itau.authorizer.authorization.application.adapter.`in`.grpc

import com.itau.authorizer.common.application.adapter.mapper.toCurrentBalanceResponse
import com.itau.authorizer.authorization.application.model.grpc.CurrentBalanceRPCGrpcKt
import com.itau.authorizer.authorization.application.model.grpc.CurrentBalanceRequest
import com.itau.authorizer.authorization.application.model.grpc.CurrentBalanceResponse
import com.itau.authorizer.authorization.domain.usecase.CurrentBalanceUsecase
import java.util.UUID
import org.springframework.grpc.server.service.GrpcService

@GrpcService
class CurrentBalanceRetrieverRPC(
    private val currentBalanceUsecase: CurrentBalanceUsecase
) : CurrentBalanceRPCGrpcKt.CurrentBalanceRPCCoroutineImplBase() {

    override suspend fun accountCurrentBalance(
        request: CurrentBalanceRequest,
    ): CurrentBalanceResponse = try{
        currentBalanceUsecase.accountCurrentBalance(
            UUID.fromString(request.accountId)
        ).toCurrentBalanceResponse()
    } catch (e: Exception) {
        e.printStackTrace()
        throw e
    }


}
