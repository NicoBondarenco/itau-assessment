package com.itau.authorizer.authorization.domain.port.`in`

import com.itau.authorizer.common.domain.model.entity.AccountTransactionEntity

interface ExecuteTransaction {

    suspend fun executeTransaction(entity: AccountTransactionEntity): AccountTransactionEntity

}
