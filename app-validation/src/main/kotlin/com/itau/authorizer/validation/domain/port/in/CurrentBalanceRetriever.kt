package com.itau.authorizer.validation.domain.port.`in`

import com.itau.authorizer.common.domain.model.entity.CurrentBalanceEntity
import java.util.UUID

interface CurrentBalanceRetriever {

    suspend fun accountCurrentBalance(accountId: UUID): CurrentBalanceEntity

}
