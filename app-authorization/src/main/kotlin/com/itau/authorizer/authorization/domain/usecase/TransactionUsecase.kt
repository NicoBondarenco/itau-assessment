package com.itau.authorizer.authorization.domain.usecase

import com.itau.authorizer.authorization.domain.model.entity.TransactionEntity
import com.itau.authorizer.authorization.domain.port.`in`.TransactionExecution
import org.springframework.stereotype.Service

@Service
class TransactionUsecase(
    private val transactionExecution: TransactionExecution
) {

    suspend fun executeTransaction(entity: TransactionEntity) {
        transactionExecution.executeTransaction()
    }

}
