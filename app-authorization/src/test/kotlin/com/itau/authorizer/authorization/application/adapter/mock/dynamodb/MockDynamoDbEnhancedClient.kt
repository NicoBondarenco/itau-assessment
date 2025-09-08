package com.itau.authorizer.authorization.application.adapter.mock.dynamodb

import com.itau.authorizer.common.application.mapper.accountTable
import com.itau.authorizer.common.application.mapper.accountTransactions
import com.itau.authorizer.common.application.mapper.balanceTable
import com.itau.authorizer.common.application.mapper.transactionTable
import com.itau.authorizer.common.application.model.dynamodb.AccountDynamoDB
import com.itau.authorizer.common.application.model.dynamodb.BalanceDynamoDB
import com.itau.authorizer.common.application.model.dynamodb.TransactionDynamoDB
import com.itau.authorizer.common.infrastructure.resolver.AppTableNameResolver
import com.itau.authorizer.common.util.extension.toUUID
import io.mockk.Call
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME
import java.util.UUID
import java.util.function.Consumer
import java.util.stream.Stream
import software.amazon.awssdk.core.pagination.sync.SdkIterable
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClientExtension
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.extensions.WriteModification
import software.amazon.awssdk.enhanced.dynamodb.internal.conditional.BetweenConditional
import software.amazon.awssdk.enhanced.dynamodb.model.Page
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest

abstract class MockDynamoDbEnhancedClient : DynamoDbEnhancedClient {

    companion object {
        val INSTANCE: MockDynamoDbEnhancedClient
            get() = spyk<MockDynamoDbEnhancedClient>().apply {
                val accountTableMock = mockDynamoDbTable(AccountDynamoDB::class.java, this.accountsDynamoDB)
                val balanceTableMock = mockDynamoDbTable(BalanceDynamoDB::class.java, this.balancesDynamoDB)
                val transactionTableMock = mockDynamoDbTable(TransactionDynamoDB::class.java, this.transactionsDynamoDB)
                    .mockDynamoDbIndex()
                val writer = mockk<TransactWriteItemsEnhancedRequest.Builder>()
                    .mockAddItem(accountTableMock, accountsDynamoDB) { it.accountId }
                    .mockAddItem(balanceTableMock, balancesDynamoDB) { it.accountId }
                    .mockAddItem(transactionTableMock, transactionsDynamoDB) { it.transactionId }

                every { accountTable } returns accountTableMock
                every { balanceTable } returns balanceTableMock
                every { transactionTable } returns transactionTableMock
                every { transactWriteItems(any<Consumer<TransactWriteItemsEnhancedRequest.Builder>>()) } answers { call ->
                    (call.invocation.args[0] as Consumer<TransactWriteItemsEnhancedRequest.Builder>).let {
                        it.accept(writer)
                        mockk<Void>()
                    }
                }
            }
    }

    val accountsDynamoDB = mutableMapOf<UUID, AccountDynamoDB>()
    val balancesDynamoDB = mutableMapOf<UUID, BalanceDynamoDB>()
    val transactionsDynamoDB = mutableMapOf<UUID, TransactionDynamoDB>()

    fun clear() {
        accountsDynamoDB.clear()
        balancesDynamoDB.clear()
        transactionsDynamoDB.clear()
    }

    fun <T : Any> put(item: T) = item.apply {
        when (this) {
            is AccountDynamoDB -> accountsDynamoDB[this.accountId] = this
            is BalanceDynamoDB -> balancesDynamoDB[this.accountId] = this
            is TransactionDynamoDB -> transactionsDynamoDB[this.transactionId] = this
        }
    }

    inline fun <reified T : Any> get(id: UUID): T? = when (T::class) {
        AccountDynamoDB::class -> accountsDynamoDB[id] as T?
        BalanceDynamoDB::class -> balancesDynamoDB[id] as T?
        TransactionDynamoDB::class -> transactionsDynamoDB[id] as T?
        else -> null
    }

    fun <T : Any> mockDynamoDbTable(
        clazz: Class<T>,
        map: MutableMap<UUID, T>,
    ): DynamoDbTable<T> = mockk<DynamoDbTable<T>>().apply {
        val nameResolver = AppTableNameResolver()
        every { tableSchema() } returns TableSchema.fromBean(clazz)
        every { tableName() } returns nameResolver.resolve(clazz)
        every { mapperExtension() } returns mockk<DynamoDbEnhancedClientExtension>().apply {
            every { beforeWrite(any()) } returns mockk<WriteModification>()
        }
        every { getItem(any<Key>()) } answers { get ->
            val key = (get.invocation.args[0] as Key).partitionKeyValue().s().let { UUID.fromString(it) }
            map[key]
        }
    }

    fun DynamoDbTable<TransactionDynamoDB>.mockDynamoDbTransactions(): DynamoDbTable<TransactionDynamoDB> = this.apply {
        every { accountTransactions(any<UUID>(), any<Pair<ZonedDateTime, ZonedDateTime>>()) } answers {
            val period = secondArg<Pair<ZonedDateTime, ZonedDateTime>>()
            transactionsDynamoDB.values.filter { it.accountId == firstArg() && it.timestamp >= period.first && it.timestamp <= period.second }
        }
    }

    fun DynamoDbTable<TransactionDynamoDB>.mockDynamoDbIndex(): DynamoDbTable<TransactionDynamoDB> = this.apply {
        every { this@mockDynamoDbIndex.index(any()) } returns mockk<DynamoDbIndex<TransactionDynamoDB>>().apply {
            every { query(any<QueryEnhancedRequest>()) } answers {
                val query = firstArg<QueryEnhancedRequest>().keyAndPeriod()
                transactionsDynamoDB.values.filter {
                    it.accountId == query.first && it.timestamp >= query.second && it.timestamp <= query.third
                }.let { iterable(it) }
            }
        }
    }

    private fun QueryEnhancedRequest.keyAndPeriod(): Triple<UUID, ZonedDateTime, ZonedDateTime> {
        val condition = (this.queryConditional() as BetweenConditional)
        val key1 = condition.key("key1")
        val key2 = condition.key("key2")
        val accountId = key1.partitionKeyValue().s().toUUID()
        val start = ZonedDateTime.parse(key1.sortKeyValue().get().s(), ISO_ZONED_DATE_TIME)
        val end = ZonedDateTime.parse(key2.sortKeyValue().get().s(), ISO_ZONED_DATE_TIME)
        return Triple(accountId, start, end)
    }

    private fun BetweenConditional.key(name: String) = BetweenConditional::class.java
        .getDeclaredField(name).apply {
            trySetAccessible()
        }.get(this) as Key

    inline fun <reified T : Any> TransactWriteItemsEnhancedRequest.Builder.mockAddItem(
        tableMock: DynamoDbTable<T>,
        map: MutableMap<UUID, T>,
        crossinline itemId: (T) -> UUID,
    ) = this.apply {
        every { addPutItem(tableMock, any<T>()) } answers { put ->
            put.setItem(map, this@mockAddItem, itemId)
        }
        every { addUpdateItem(tableMock, any<T>()) } answers { put ->
            put.setItem(map, this@mockAddItem, itemId)
        }
    }

    inline fun <reified T : Any> Call.setItem(
        map: MutableMap<UUID, T>,
        builder: TransactWriteItemsEnhancedRequest.Builder,
        crossinline itemId: (T) -> UUID,
    ): TransactWriteItemsEnhancedRequest.Builder = (this.invocation.args[1] as T).let { item ->
        map[itemId(item)] = item
        builder
    }

    fun iterable(list: List<TransactionDynamoDB>): SdkIterable<Page<TransactionDynamoDB>> = object : SdkIterable<Page<TransactionDynamoDB>> {

        override fun iterator(): MutableIterator<Page<TransactionDynamoDB>?> = stream().iterator()

        override fun stream(): Stream<Page<TransactionDynamoDB>> = Stream
            .of(Page.builder(TransactionDynamoDB::class.java).items(list).build())

    }

}
