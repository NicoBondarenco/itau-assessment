package com.itau.authorizer.common.application.mapper

import com.itau.authorizer.common.application.model.grpc.CurrentBalanceResponse
import com.itau.authorizer.common.domain.model.entity.CurrentBalanceEntity
import com.itau.authorizer.common.util.extension.toAmountFormat

fun CurrentBalanceEntity.toCurrentBalanceResponse(): CurrentBalanceResponse = CurrentBalanceResponse.newBuilder()
    .setAccountId(this.accountId.toString())
    .setCurrentBalance(this.currentBalance.toAmountFormat())
    .setDailyTransacted(this.dailyTransacted.toAmountFormat())
    .build()
