package com.itau.authorizer.authorization.infrastructure.configuration

import com.itau.authorizer.common.application.adapter.`in`.grpc.ExecuteTransactionGrpcKt.ExecuteTransactionCoroutineStub
import com.itau.authorizer.common.application.adapter.out.grpc.RetrieveCurrentBalanceGrpcKt.RetrieveCurrentBalanceCoroutineStub
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.grpc.client.GrpcChannelFactory

@Configuration
class TestGRpcConfiguration {

    @Bean
    fun currentBalance(
        channels: GrpcChannelFactory
    ): RetrieveCurrentBalanceCoroutineStub =
        RetrieveCurrentBalanceCoroutineStub(
            channels.createChannel("current-balance")
        )

    @Bean
    fun executeTransaction(
        channels: GrpcChannelFactory
    ): ExecuteTransactionCoroutineStub =
        ExecuteTransactionCoroutineStub(
            channels.createChannel("execute-transaction")
        )

}
