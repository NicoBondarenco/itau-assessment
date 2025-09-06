package com.itau.authorizer.web.domain.port.out

import com.itau.authorizer.common.domain.model.entity.AccountEntity

interface AccountListRetrieverOut {

    fun allAccounts(): List<AccountEntity>

}
