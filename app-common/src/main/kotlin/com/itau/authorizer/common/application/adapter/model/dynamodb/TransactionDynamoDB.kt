package com.itau.authorizer.common.application.adapter.model.dynamodb

import com.itau.authorizer.common.domain.model.value.TransactionType.UNKNOWN
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.time.ZonedDateTime.now
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey

@DynamoDbBean
data class TransactionDynamoDB(
    @get:DynamoDbPartitionKey
    var transactionId: String = "",
    @get:DynamoDbSecondaryPartitionKey(indexNames = ["AccountIdIndex"])
    var accountId: String = "",
    var amount: BigDecimal = BigDecimal.ZERO,
    var type: String = UNKNOWN.name,
    @get:DynamoDbSortKey
    @get:DynamoDbSecondarySortKey(indexNames = ["AccountIdIndex"])
    var timestamp: ZonedDateTime = now(),
)
