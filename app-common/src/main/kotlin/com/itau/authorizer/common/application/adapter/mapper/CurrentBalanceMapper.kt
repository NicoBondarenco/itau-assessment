package com.itau.authorizer.common.application.adapter.mapper

import com.itau.authorizer.authorization.application.model.grpc.CurrentBalanceResponse
import com.itau.authorizer.common.domain.model.entity.CurrentBalanceEntity

fun CurrentBalanceEntity.toCurrentBalanceResponse(): CurrentBalanceResponse = CurrentBalanceResponse.newBuilder()
    .setAccountId(this.accountId.toString())
    .setCurrentBalance(this.currentBalance.toDouble())
    .setDailyTransacted(this.dailyTransacted.toDouble())
    .build()
