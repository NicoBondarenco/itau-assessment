package com.itau.authorizer.common.application.mapper

import com.itau.authorizer.common.application.model.dynamodb.AccountDynamoDB
import com.itau.authorizer.common.application.model.dynamodb.BalanceDynamoDB
import com.itau.authorizer.common.application.model.dynamodb.TransactionDynamoDB
import com.itau.authorizer.common.infrastructure.resolver.AppTableNameResolver
import com.itau.authorizer.common.util.extension.toIsoFormat
import java.time.ZonedDateTime
import java.util.UUID
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest

private val tableNameResolver: AppTableNameResolver = AppTableNameResolver()

val DynamoDbEnhancedClient.accountTable: DynamoDbTable<AccountDynamoDB>
    get() = this.table(
        tableNameResolver.resolve(AccountDynamoDB::class.java),
        TableSchema.fromBean(AccountDynamoDB::class.java)
    )

val DynamoDbEnhancedClient.balanceTable: DynamoDbTable<BalanceDynamoDB>
    get() = this.table(
        tableNameResolver.resolve(BalanceDynamoDB::class.java),
        TableSchema.fromBean(BalanceDynamoDB::class.java)
    )

val DynamoDbEnhancedClient.transactionTable: DynamoDbTable<TransactionDynamoDB>
    get() = this.table(
        tableNameResolver.resolve(TransactionDynamoDB::class.java),
        TableSchema.fromBean(TransactionDynamoDB::class.java)
    )

val DynamoDbTable<TransactionDynamoDB>.accountIndex: DynamoDbIndex<TransactionDynamoDB>
    get() = this.index("AccountIdIndex")

fun DynamoDbTable<TransactionDynamoDB>.accountTransactions(
    accountId: UUID,
    timestampRange: Pair<ZonedDateTime, ZonedDateTime>,
): List<TransactionDynamoDB> = this.accountIndex.accountTransactions(accountId, timestampRange)

fun DynamoDbIndex<TransactionDynamoDB>.accountTransactions(
    accountId: UUID,
    timestampRange: Pair<ZonedDateTime, ZonedDateTime>,
): List<TransactionDynamoDB> = this.list(accountTransactionsRequest(accountId, timestampRange))

private fun accountTransactionsRequest(
    accountId: UUID,
    range: Pair<ZonedDateTime, ZonedDateTime>,
) = QueryEnhancedRequest.builder()
    .queryConditional(accountTransactionsCondition(accountId, range))
    .build()

private fun accountTransactionsCondition(
    accountId: UUID,
    range: Pair<ZonedDateTime, ZonedDateTime>,
): QueryConditional = QueryConditional.sortBetween(
    transactionAccountKey(accountId, range.first),
    transactionAccountKey(accountId, range.second)
)

private fun transactionAccountKey(
    accountId: UUID,
    timestamp: ZonedDateTime
): Key = Key.builder().partitionKey(accountId, timestamp.toIsoFormat())

fun <T : Any, K : Any> DynamoDbTable<T>.one(
    key: K
): T? = this.getItem(Key.builder().partitionKey(key, null))

fun <T : Any> DynamoDbIndex<T>.list(
    query: QueryEnhancedRequest
): List<T> = this.query(query)
    .stream()
    .map { it.items() }
    .toList()
    .flatten()

fun <H : Any, R : Any> Key.Builder.partitionKey(
    partitionKey: H,
    sortKey: R?,
): Key = this.partitionValue(partitionKey.toString())
    .apply { sortKey?.let { sortValue(it.toString()) } }
    .build()
