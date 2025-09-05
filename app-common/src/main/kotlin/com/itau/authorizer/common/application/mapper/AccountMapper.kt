package com.itau.authorizer.common.application.mapper

import com.itau.authorizer.common.application.model.dynamodb.AccountDynamoDB
import com.itau.authorizer.common.domain.model.entity.AccountEntity

fun AccountDynamoDB.toAccountEntity() = AccountEntity(
    accountId = this.accountId,
    createdAt = this.createdAt,
    isActive = this.isActive,
    dailyLimit = this.dailyLimit,
)

fun AccountEntity.toAccountDynamoDB() = AccountDynamoDB(
    accountId = this.accountId,
    createdAt = this.createdAt,
    isActive = this.isActive,
    dailyLimit = this.dailyLimit,
)
