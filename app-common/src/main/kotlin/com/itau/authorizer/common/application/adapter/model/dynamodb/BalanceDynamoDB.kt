package com.itau.authorizer.common.application.adapter.model.dynamodb

import java.math.BigDecimal
import java.time.ZonedDateTime
import java.time.ZonedDateTime.now
import java.util.UUID
import java.util.UUID.randomUUID
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey

@DynamoDbBean
data class BalanceDynamoDB(
    @get:DynamoDbPartitionKey
    var accountId: UUID = randomUUID(),
    var amount: BigDecimal = BigDecimal.ZERO,
    @get:DynamoDbSortKey
    var lastUpdate: ZonedDateTime = now(),
)
