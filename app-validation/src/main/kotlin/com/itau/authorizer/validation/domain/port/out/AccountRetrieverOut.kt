package com.itau.authorizer.validation.domain.port.out

import com.itau.authorizer.common.domain.model.entity.AccountEntity
import java.util.UUID

interface AccountRetrieverOut {

    suspend fun retrieveAccount(accountId: UUID): AccountEntity

}
