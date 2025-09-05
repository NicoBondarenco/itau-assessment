package com.itau.authorizer.validation.domain.usecase

import com.itau.authorizer.common.domain.model.entity.AccountEntity
import com.itau.authorizer.common.domain.model.entity.CurrentBalanceEntity
import com.itau.authorizer.common.domain.model.entity.TransactionEntity
import com.itau.authorizer.validation.domain.exception.InactiveAccountException
import com.itau.authorizer.validation.domain.exception.InsufficientFundsException
import com.itau.authorizer.validation.domain.exception.InvalidAmountException
import com.itau.authorizer.validation.domain.exception.LimitReachedException
import com.itau.authorizer.validation.domain.port.`in`.CurrentBalanceRetriever
import com.itau.authorizer.validation.domain.port.out.AccountRetriever
import com.itau.authorizer.validation.domain.port.out.TransactionExecutor
import java.math.BigDecimal
import org.springframework.stereotype.Service

@Service
class TransactionUsecase(
    private val currentBalanceRetriever: CurrentBalanceRetriever,
    private val transactionExecutor: TransactionExecutor,
    private val accountRetriever: AccountRetriever,
) {

    suspend fun executeTransaction(transaction: TransactionEntity) {
        transaction.validate()
        transactionExecutor.executeTransaction(transaction)
    }

    private suspend fun TransactionEntity.validate() {
        validateAmount()

        val account = accountRetriever.retrieveAccount(accountId)
        account.validateActive()

        val balance = currentBalanceRetriever.accountCurrentBalance(account.accountId)
        validateBalance(balance)
        validateLimit(balance, account)
    }

    private suspend fun AccountEntity.validateActive() {
        if (!isActive) {
            throw InactiveAccountException(accountId)
        }
    }

    private suspend fun TransactionEntity.validateAmount() {
        if (amount <= BigDecimal.ZERO) {
            throw InvalidAmountException(accountId, transactionId)
        }
    }

    private suspend fun TransactionEntity.validateBalance(
        balance: CurrentBalanceEntity,
    ) {
        if (balance.currentBalance.minus(this.amount) < BigDecimal.ZERO) {
            throw InsufficientFundsException(accountId, transactionId)
        }
    }

    private suspend fun TransactionEntity.validateLimit(
        balance: CurrentBalanceEntity,
        account: AccountEntity,
    ) {
        if (balance.dailyTransacted.plus(this.amount) > account.dailyLimit) {
            throw LimitReachedException(accountId, transactionId)
        }
    }

}
