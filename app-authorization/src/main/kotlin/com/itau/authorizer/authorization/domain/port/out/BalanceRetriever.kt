package com.itau.authorizer.authorization.domain.port.out

import com.itau.authorizer.authorization.domain.model.entity.CurrentBalanceEntity
import java.util.UUID

interface BalanceRetriever {

    suspend fun accountBalance(accountId: UUID): BalanceEntity

}
