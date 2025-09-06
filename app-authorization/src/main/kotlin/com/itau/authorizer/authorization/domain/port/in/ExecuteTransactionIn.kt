package com.itau.authorizer.authorization.domain.port.`in`

import com.itau.authorizer.common.domain.model.entity.AccountTransactionEntity

interface ExecuteTransactionIn {

    suspend fun executeTransaction(entity: AccountTransactionEntity): AccountTransactionEntity

}
