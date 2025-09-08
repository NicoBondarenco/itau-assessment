package com.itau.authorizer.validation.infrastructure.configuration

import com.itau.authorizer.common.application.adapter.`in`.grpc.ExecuteTransactionGrpcKt.ExecuteTransactionCoroutineStub
import com.itau.authorizer.common.application.adapter.out.grpc.RetrieveCurrentBalanceGrpcKt.RetrieveCurrentBalanceCoroutineStub
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.grpc.client.GrpcChannelFactory

@Configuration
@Profile("!test")
class GRpcConfiguration {

    @Bean
    fun currentBalance(
        channels: GrpcChannelFactory
    ): RetrieveCurrentBalanceCoroutineStub = RetrieveCurrentBalanceCoroutineStub(
        channels.createChannel("current-balance")
    )

    @Bean
    fun executeTransaction(
        channels: GrpcChannelFactory
    ): ExecuteTransactionCoroutineStub = ExecuteTransactionCoroutineStub(
        channels.createChannel("execute-transaction")
    )

}
