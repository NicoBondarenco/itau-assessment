package com.itau.authorizer.web.domain.port.`in`

import com.itau.authorizer.common.domain.model.entity.AccountDataEntity

interface AccountBatchSaver {

    suspend fun saveAll(accounts: List<AccountDataEntity>)

}
