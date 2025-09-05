package com.itau.authorizer.common.application.model.dynamodb

import com.itau.authorizer.common.domain.model.value.TransactionType
import com.itau.authorizer.common.domain.model.value.TransactionType.UNKNOWN
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.time.ZonedDateTime.now
import java.util.UUID
import java.util.UUID.randomUUID
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey

@DynamoDbBean
data class TransactionDynamoDB(
    @get:DynamoDbPartitionKey
    var transactionId: UUID = randomUUID(),
    @get:DynamoDbSecondaryPartitionKey(indexNames = ["AccountIdIndex"])
    var accountId: UUID = randomUUID(),
    var amount: BigDecimal = BigDecimal.ZERO,
    var type: TransactionType = UNKNOWN,
    @get:DynamoDbSecondarySortKey(indexNames = ["AccountIdIndex"])
    var timestamp: ZonedDateTime = now(),
)
