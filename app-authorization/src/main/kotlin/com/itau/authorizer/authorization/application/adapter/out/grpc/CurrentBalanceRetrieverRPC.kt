package com.itau.authorizer.authorization.application.adapter.out.grpc

import com.itau.authorizer.authorization.domain.usecase.RetrieveCurrentBalanceUsecase
import com.itau.authorizer.common.application.adapter.out.grpc.RetrieveCurrentBalanceGrpcKt.RetrieveCurrentBalanceCoroutineImplBase
import com.itau.authorizer.common.application.mapper.toCurrentBalanceResponse
import com.itau.authorizer.common.application.model.grpc.CurrentBalanceRequest
import com.itau.authorizer.common.application.model.grpc.CurrentBalanceResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.UUID
import org.springframework.grpc.server.service.GrpcService

@GrpcService
class CurrentBalanceRetrieverRPC(
    private val retrieveCurrentBalanceUsecase: RetrieveCurrentBalanceUsecase
) : RetrieveCurrentBalanceCoroutineImplBase() {

    private val logger = KotlinLogging.logger { }

    override suspend fun accountCurrentBalance(
        request: CurrentBalanceRequest,
    ): CurrentBalanceResponse = try {
        retrieveCurrentBalanceUsecase.accountCurrentBalance(
            UUID.fromString(request.accountId)
        ).toCurrentBalanceResponse()
    } catch (e: Exception) {
        logger.error(e) { "Error while retrieving account current balance: ${e.message}" }
        throw e
    }


}
