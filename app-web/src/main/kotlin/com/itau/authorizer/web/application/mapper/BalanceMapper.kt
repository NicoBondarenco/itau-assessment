package com.itau.authorizer.web.application.mapper

import com.itau.authorizer.common.application.adapter.model.dynamodb.BalanceDynamoDB
import com.itau.authorizer.common.domain.model.entity.BalanceEntity

fun BalanceEntity.toBalanceDynamoDB() = BalanceDynamoDB(
    accountId = accountId,
    amount = amount,
    lastUpdate = lastUpdate,
)
