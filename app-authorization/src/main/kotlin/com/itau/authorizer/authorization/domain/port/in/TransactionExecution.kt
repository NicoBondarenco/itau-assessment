package com.itau.authorizer.authorization.domain.port.`in`

import com.itau.authorizer.authorization.domain.model.entity.AccountTransactionEntity

interface TransactionExecution {

    fun executeTransaction(entity: AccountTransactionEntity)

}
