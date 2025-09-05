package com.itau.authorizer.common.application.mapper

import com.itau.authorizer.common.application.model.grpc.CurrentBalanceRequest
import com.itau.authorizer.common.application.model.grpc.CurrentBalanceResponse
import com.itau.authorizer.common.domain.model.entity.CurrentBalanceEntity
import com.itau.authorizer.common.util.extension.toAmountFormat
import java.util.UUID

fun CurrentBalanceEntity.toCurrentBalanceResponse(): CurrentBalanceResponse = CurrentBalanceResponse.newBuilder()
    .setAccountId(this.accountId.toString())
    .setCurrentBalance(this.currentBalance.toAmountFormat())
    .setDailyTransacted(this.dailyTransacted.toAmountFormat())
    .build()

fun UUID.toCurrentBalanceRequest(): CurrentBalanceRequest = CurrentBalanceRequest
    .newBuilder().setAccountId(this.toString()).build()

fun CurrentBalanceResponse.toCurrentBalanceEntity() = CurrentBalanceEntity(
    accountId = UUID.fromString(this.accountId),
    currentBalance = this.currentBalance.toBigDecimal(),
    dailyTransacted = this.dailyTransacted.toBigDecimal(),
)
