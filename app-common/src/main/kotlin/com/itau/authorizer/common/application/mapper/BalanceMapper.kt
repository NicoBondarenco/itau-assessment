package com.itau.authorizer.common.application.mapper

import com.itau.authorizer.common.application.model.dynamodb.BalanceDynamoDB
import com.itau.authorizer.common.domain.model.entity.BalanceEntity

fun BalanceEntity.toBalanceDynamoDB() = BalanceDynamoDB(
    accountId = accountId,
    amount = amount,
    lastUpdate = lastUpdate,
)

fun BalanceDynamoDB.toBalanceEntity() = BalanceEntity(
    accountId = this.accountId,
    amount = this.amount,
    lastUpdate = this.lastUpdate,
)
