package com.itau.authorizer.authorization.domain.port.out

import com.itau.authorizer.authorization.domain.model.entity.CurrentBalanceEntity
import java.util.UUID

interface CurrentBalanceRetriever {

    suspend fun accountCurrentBalance(accountId: UUID): CurrentBalanceEntity

}
