package com.itau.authorizer.authorization.domain.port.out

import com.itau.authorizer.common.domain.model.entity.CurrentBalanceEntity
import java.util.UUID

interface CurrentBalanceRetrieverOut {

    suspend fun accountCurrentBalance(accountId: UUID): CurrentBalanceEntity

}
