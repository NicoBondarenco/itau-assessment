package com.itau.authorizer.validation.domain.port.out

import com.itau.authorizer.common.domain.model.entity.TransactionEntity

interface TransactionExecutorOut {

    suspend fun executeTransaction(entity: TransactionEntity)

}
