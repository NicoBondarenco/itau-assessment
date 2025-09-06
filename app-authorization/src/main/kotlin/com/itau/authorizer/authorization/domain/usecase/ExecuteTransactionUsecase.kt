package com.itau.authorizer.authorization.domain.usecase

import com.itau.authorizer.authorization.domain.model.mapper.toAccountTransactionEntity
import com.itau.authorizer.authorization.domain.port.`in`.ExecuteTransactionIn
import com.itau.authorizer.authorization.domain.port.out.BalanceRetrieverOut
import com.itau.authorizer.authorization.domain.port.out.TransactionExecutedProducerOut
import com.itau.authorizer.common.domain.model.entity.TransactionEntity
import org.springframework.stereotype.Service

@Service
class ExecuteTransactionUsecase(
    private val executeTransaction: ExecuteTransactionIn,
    private val balanceRetriever: BalanceRetrieverOut,
    private val transactionExecutedProducer: TransactionExecutedProducerOut,
) {

    suspend fun executeTransaction(entity: TransactionEntity) {
        val balanceAmount = balanceRetriever.accountBalance(entity.accountId)
            .amount.minus(entity.amount)
        val accountTransaction = entity.toAccountTransactionEntity(balanceAmount)
        executeTransaction.executeTransaction(accountTransaction)
        transactionExecutedProducer.produce(accountTransaction)
    }

}
