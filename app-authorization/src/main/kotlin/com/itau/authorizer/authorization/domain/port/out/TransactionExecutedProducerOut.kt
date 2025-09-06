package com.itau.authorizer.authorization.domain.port.out

import com.itau.authorizer.common.domain.model.entity.AccountTransactionEntity

interface TransactionExecutedProducerOut {

    suspend fun produce(entity: AccountTransactionEntity)

}
