package com.itau.authorizer.authorization.domain.usecase

import com.itau.authorizer.authorization.domain.port.out.CurrentBalanceRetrieverOut
import com.itau.authorizer.common.domain.model.entity.CurrentBalanceEntity
import java.util.UUID
import org.springframework.stereotype.Service

@Service
class RetrieveCurrentBalanceUsecase(
    private val currentBalanceRetriever: CurrentBalanceRetrieverOut
) {

    suspend fun accountCurrentBalance(
        accountId: UUID,
    ): CurrentBalanceEntity = currentBalanceRetriever.accountCurrentBalance(accountId)

}
