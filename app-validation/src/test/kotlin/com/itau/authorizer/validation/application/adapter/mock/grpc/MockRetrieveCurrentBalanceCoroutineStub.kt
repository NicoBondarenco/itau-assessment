package com.itau.authorizer.validation.application.adapter.mock.grpc

import com.itau.authorizer.common.application.adapter.out.grpc.RetrieveCurrentBalanceGrpcKt.RetrieveCurrentBalanceCoroutineStub
import com.itau.authorizer.common.application.model.grpc.CurrentBalanceRequest
import com.itau.authorizer.common.application.model.grpc.CurrentBalanceResponse
import com.itau.authorizer.common.util.extension.toUUID
import io.grpc.Status
import io.mockk.coEvery
import io.mockk.mockk
import java.util.UUID

val currentBalancesMock = mutableMapOf<UUID, CurrentBalanceResponse>()


fun mockRetrieveCurrentBalanceCoroutineStub() = mockk<RetrieveCurrentBalanceCoroutineStub>().apply {
    coEvery { accountCurrentBalance(any(), any()) } answers {
        val accountId = firstArg<CurrentBalanceRequest>().accountId.toUUID()
        currentBalancesMock[accountId]
            ?: throw Status.NOT_FOUND
                .withDescription("Account $accountId has no balance")
                .asException()
    }
}
