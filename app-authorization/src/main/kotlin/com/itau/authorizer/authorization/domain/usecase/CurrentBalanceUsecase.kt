package com.itau.authorizer.authorization.domain.usecase

import com.itau.authorizer.authorization.domain.model.entity.CurrentBalanceEntity
import com.itau.authorizer.authorization.domain.port.out.CurrentBalanceRetriever
import java.util.UUID
import org.springframework.stereotype.Service

@Service
class CurrentBalanceUsecase(
    private val currentBalanceRetriever: CurrentBalanceRetriever
) {

    suspend fun accountCurrentBalance(
        accountId: UUID,
    ): CurrentBalanceEntity = currentBalanceRetriever.accountCurrentBalance(accountId)

}
