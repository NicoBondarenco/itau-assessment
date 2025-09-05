package com.itau.authorizer.web.domain.port.`in`

import com.itau.authorizer.common.domain.model.entity.AccountDataEntity

interface AccountBatchSaver {

    fun saveAll(accounts: List<AccountDataEntity>)

}
