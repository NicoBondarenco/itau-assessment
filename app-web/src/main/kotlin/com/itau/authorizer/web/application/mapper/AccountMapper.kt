package com.itau.authorizer.web.application.mapper

import com.itau.authorizer.common.domain.model.entity.AccountDataEntity
import com.itau.authorizer.common.domain.model.entity.AccountEntity
import com.itau.authorizer.common.domain.model.entity.BalanceEntity
import com.itau.authorizer.common.domain.model.entity.TransactionEntity
import com.itau.authorizer.web.application.model.rest.AccountDataResponse
import com.itau.authorizer.web.application.model.rest.AccountResponse
import com.itau.authorizer.web.application.model.rest.BalanceResponse
import com.itau.authorizer.web.application.model.rest.TransactionResponse

fun AccountDataEntity.toAccountDataResponse() = AccountDataResponse(
    account = this.account.toAccountResponse(),
    balance = this.balance.toBalanceResponse(),
    transactions = this.transactions.map { it.toTransactionResponse() }
)

fun AccountEntity.toAccountResponse() = AccountResponse(
    accountId = this.accountId,
    createdAt = this.createdAt,
    isActive = this.isActive,
    dailyLimit = this.dailyLimit,
)

fun BalanceEntity.toBalanceResponse() = BalanceResponse(
    accountId = this.accountId,
    amount = this.amount,
    lastUpdate = this.lastUpdate,
)

fun TransactionEntity.toTransactionResponse() = TransactionResponse(
    transactionId = this.transactionId,
    accountId = this.accountId,
    amount = this.amount,
    type = this.type,
    timestamp = this.timestamp,
)
