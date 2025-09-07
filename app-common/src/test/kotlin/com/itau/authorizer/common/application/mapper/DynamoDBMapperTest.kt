package com.itau.authorizer.common.application.mapper

import com.itau.authorizer.common.application.model.dynamodb.AccountDynamoDB
import com.itau.authorizer.common.application.model.dynamodb.BalanceDynamoDB
import com.itau.authorizer.common.application.model.dynamodb.TransactionDynamoDB
import com.itau.authorizer.common.infrastructure.resolver.AppTableNameResolver
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import java.time.ZonedDateTime
import java.util.UUID
import java.util.stream.Stream
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.mapper.BeanTableSchema
import software.amazon.awssdk.enhanced.dynamodb.model.Page
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest

class DynamoDBMapperTest {

    private val mockClient = mockk<DynamoDbEnhancedClient>()
    private val mockAccountTable = mockk<DynamoDbTable<AccountDynamoDB>>()
    private val mockBalanceTable = mockk<DynamoDbTable<BalanceDynamoDB>>()
    private val mockTransactionTable = mockk<DynamoDbTable<TransactionDynamoDB>>()
    private val mockTransactionIndex = mockk<DynamoDbIndex<TransactionDynamoDB>>()
    private val mockPageIterable = mockk<PageIterable<TransactionDynamoDB>>()
    private val mockStream = mockk<Stream<Page<TransactionDynamoDB>>>()

    @BeforeEach
    fun setup() {
        mockkConstructor(AppTableNameResolver::class)
        every { anyConstructed<AppTableNameResolver>().resolve(AccountDynamoDB::class.java) } returns "accounts"
        every { anyConstructed<AppTableNameResolver>().resolve(BalanceDynamoDB::class.java) } returns "balances"
        every { anyConstructed<AppTableNameResolver>().resolve(TransactionDynamoDB::class.java) } returns "transactions"
    }

    @AfterEach
    fun cleanup() {
        unmockkAll()
    }

    @Test
    fun `accountTable should return configured DynamoDB table`() {
        every { mockClient.table(any(), allAny<BeanTableSchema<AccountDynamoDB>>()) } returns mockAccountTable

        val result = mockClient.accountTable

        assertEquals(mockAccountTable, result)
    }

    @Test
    fun `balanceTable should return configured DynamoDB table`() {
        every { mockClient.table(any(), allAny<BeanTableSchema<BalanceDynamoDB>>()) } returns mockBalanceTable

        val result = mockClient.balanceTable

        assertEquals(mockBalanceTable, result)
    }

    @Test
    fun `transactionTable should return configured DynamoDB table`() {
        every { mockClient.table(any(), allAny<BeanTableSchema<TransactionDynamoDB>>()) } returns mockTransactionTable

        val result = mockClient.transactionTable

        assertEquals(mockTransactionTable, result)
    }

    @Test
    fun `accountIndex should return AccountIdIndex`() {
        every { mockTransactionTable.index("AccountIdIndex") } returns mockTransactionIndex

        val result = mockTransactionTable.accountIndex

        assertEquals(mockTransactionIndex, result)
    }

    @Test
    fun `accountTransactions from table should query account transactions within timestamp range`() {
        val accountId = UUID.randomUUID()
        val startTime = ZonedDateTime.now().minusHours(1)
        val endTime = ZonedDateTime.now()
        val item = mockk<TransactionDynamoDB>()
        val page = mockk<Page<TransactionDynamoDB>>().apply { every { items() } returns listOf(item) }
        val mockTransactions = listOf(page)

        every { mockTransactionTable.index("AccountIdIndex") } returns mockTransactionIndex
        every { mockTransactionIndex.query(any<QueryEnhancedRequest>()) } returns mockPageIterable
        every { mockPageIterable.stream() } returns mockStream
        every { mockStream.map<List<TransactionDynamoDB>>(any()) } returns mockTransactions.stream().map { it.items() }
        every { mockStream.toList() } returns mockTransactions

        val result = mockTransactionTable.accountTransactions(accountId, Pair(startTime, endTime))

        assertEquals(page.items(), result)
    }

    @Test
    fun `accountTransactions from index should query account transactions within timestamp range`() {
        val accountId = UUID.randomUUID()
        val startTime = ZonedDateTime.now().minusHours(1)
        val endTime = ZonedDateTime.now()
        val item = mockk<TransactionDynamoDB>()
        val page = mockk<Page<TransactionDynamoDB>>().apply { every { items() } returns listOf(item) }
        val mockTransactions = listOf(page)

        every { mockTransactionIndex.query(any<QueryEnhancedRequest>()) } returns mockPageIterable
        every { mockPageIterable.stream() } returns mockStream
        every { mockStream.map<List<TransactionDynamoDB>>(any()) } returns mockTransactions.stream().map { it.items() }
        every { mockStream.toList() } returns mockTransactions

        val result = mockTransactionIndex.accountTransactions(accountId, Pair(startTime, endTime))

        assertEquals(page.items(), result)
    }

    @Test
    fun `accountTransactions should handle empty results`() {
        val accountId = UUID.randomUUID()
        val startTime = ZonedDateTime.now().minusHours(1)
        val endTime = ZonedDateTime.now()

        every { mockTransactionTable.index("AccountIdIndex") } returns mockTransactionIndex
        every { mockTransactionIndex.query(any<QueryEnhancedRequest>()) } returns mockPageIterable
        every { mockPageIterable.stream() } returns mockStream
        every { mockStream.map<List<TransactionDynamoDB>>(any()) } returns Stream.empty()
        every { mockStream.toList() } returns emptyList()

        val result = mockTransactionTable.accountTransactions(accountId, Pair(startTime, endTime))

        assertEquals(emptyList<TransactionDynamoDB>(), result)
    }

    @Test
    fun `one should return item when found`() {
        val key = UUID.randomUUID()
        val mockAccount = mockk<AccountDynamoDB>()
        val expectedKey = Key.builder().partitionValue(key.toString()).build()

        every { mockAccountTable.getItem(expectedKey) } returns mockAccount

        val result = mockAccountTable.one(key)

        assertEquals(mockAccount, result)
    }

    @Test
    fun `one should return null when item not found`() {
        val key = UUID.randomUUID()
        val expectedKey = Key.builder().partitionValue(key.toString()).build()

        every { mockAccountTable.getItem(expectedKey) } returns null

        val result = mockAccountTable.one(key)

        assertNull(result)
    }

    @Test
    fun `list should return flattened list of items from pages`() {
        val mockQuery = mockk<QueryEnhancedRequest>()
        val mockTransactions1 = listOf(mockk<TransactionDynamoDB>(), mockk<TransactionDynamoDB>())
        val mockTransactions2 = listOf(mockk<TransactionDynamoDB>())
        val mockPage1 = mockk<Page<TransactionDynamoDB>>()
        val mockPage2 = mockk<Page<TransactionDynamoDB>>()

        every { mockTransactionIndex.query(mockQuery) } returns mockPageIterable
        every { mockPageIterable.stream() } returns Stream.of(mockPage1, mockPage2)
        every { mockPage1.items() } returns mockTransactions1
        every { mockPage2.items() } returns mockTransactions2

        val result = mockTransactionIndex.list(mockQuery)

        assertEquals(3, result.size)
        assertEquals(mockTransactions1 + mockTransactions2, result)
    }

    @Test
    fun `list should handle empty results`() {
        val mockQuery = mockk<QueryEnhancedRequest>()

        every { mockTransactionIndex.query(mockQuery) } returns mockPageIterable
        every { mockPageIterable.stream() } returns Stream.empty()

        val result = mockTransactionIndex.list(mockQuery)

        assertEquals(emptyList<TransactionDynamoDB>(), result)
    }

    @Test
    fun `partitionKey should build key with partition value only when sort key is null`() {
        val partitionKey = UUID.randomUUID()
        val sortKey: String? = null

        val keyBuilder = Key.builder()
        val result = keyBuilder.partitionKey(partitionKey, sortKey)

        assertNotNull(result)
    }

    @Test
    fun `partitionKey should build key with both partition and sort values when sort key is provided`() {
        val partitionKey = UUID.randomUUID()
        val sortKey = "sort-value"

        val keyBuilder = Key.builder()
        val result = keyBuilder.partitionKey(partitionKey, sortKey)

        assertNotNull(result)
    }

    @Test
    fun `partitionKey should handle different data types for partition key`() {
        val stringKey = "string-key"
        val intKey = 123
        val longKey = 456L

        val keyBuilder1 = Key.builder()
        val result1 = keyBuilder1.partitionKey(stringKey, null)

        val keyBuilder2 = Key.builder()
        val result2 = keyBuilder2.partitionKey(intKey, null)

        val keyBuilder3 = Key.builder()
        val result3 = keyBuilder3.partitionKey(longKey, null)

        assertNotNull(result1)
        assertNotNull(result2)
        assertNotNull(result3)
    }

    @Test
    fun `partitionKey should handle different data types for sort key`() {
        val partitionKey = UUID.randomUUID()
        val stringSort = "string-sort"
        val intSort = 789
        val longSort = 101112L

        val keyBuilder1 = Key.builder()
        val result1 = keyBuilder1.partitionKey(partitionKey, stringSort)

        val keyBuilder2 = Key.builder()
        val result2 = keyBuilder2.partitionKey(partitionKey, intSort)

        val keyBuilder3 = Key.builder()
        val result3 = keyBuilder3.partitionKey(partitionKey, longSort)

        assertNotNull(result1)
        assertNotNull(result2)
        assertNotNull(result3)
    }
}
