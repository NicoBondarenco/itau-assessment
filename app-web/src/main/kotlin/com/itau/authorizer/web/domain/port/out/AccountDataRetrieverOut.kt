package com.itau.authorizer.web.domain.port.out

import com.itau.authorizer.common.domain.model.entity.AccountDataEntity
import java.util.UUID

interface AccountDataRetrieverOut {

    suspend fun one(accountId: UUID): AccountDataEntity

    suspend fun all(): List<AccountDataEntity>

}
