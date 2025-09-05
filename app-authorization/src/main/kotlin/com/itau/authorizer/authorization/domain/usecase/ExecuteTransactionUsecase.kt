package com.itau.authorizer.authorization.domain.usecase

import com.itau.authorizer.authorization.domain.model.mapper.toAccountTransactionEntity
import com.itau.authorizer.authorization.domain.port.`in`.ExecuteTransaction
import com.itau.authorizer.authorization.domain.port.out.BalanceRetriever
import com.itau.authorizer.authorization.domain.port.out.TransactionExecutedProducer
import com.itau.authorizer.common.domain.model.entity.TransactionEntity
import org.springframework.stereotype.Service

@Service
class ExecuteTransactionUsecase(
    private val executeTransaction: ExecuteTransaction,
    private val balanceRetriever: BalanceRetriever,
    private val transactionExecutedProducer: TransactionExecutedProducer,
) {

    suspend fun executeTransaction(entity: TransactionEntity) {
        val balanceAmount = balanceRetriever.accountBalance(entity.accountId)
            .amount.minus(entity.amount)
        val accountTransaction = entity.toAccountTransactionEntity(balanceAmount)
        executeTransaction.executeTransaction(accountTransaction)
        transactionExecutedProducer.produce(accountTransaction)
    }

}
