package com.itau.authorizer.authorization.domain.port.out

import com.itau.authorizer.common.domain.model.entity.BalanceEntity
import java.util.UUID

interface BalanceRetriever {

    suspend fun accountBalance(accountId: UUID): BalanceEntity

}
