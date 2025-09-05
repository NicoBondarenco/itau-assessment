package com.itau.authorizer.authorization.application.adapter.out.dynamodb

import com.itau.authorizer.authorization.application.model.dynamodb.BalanceDynamoDB
import com.itau.authorizer.authorization.application.model.dynamodb.TransactionDynamoDB
import com.itau.authorizer.authorization.domain.model.entity.CurrentBalanceEntity
import com.itau.authorizer.authorization.domain.port.out.CurrentBalanceRetriever
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID
import org.springframework.stereotype.Repository
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest

@Repository
class CurrentBalanceRetrieverDynamoDB(
    client: DynamoDbEnhancedClient,
) : CurrentBalanceRetriever {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")

    private val transactionTable: DynamoDbTable<TransactionDynamoDB> = client.table(
        "transaction",
        TableSchema.fromBean(TransactionDynamoDB::class.java)
    )

    private val balanceTable: DynamoDbTable<BalanceDynamoDB> = client.table(
        "balance",
        TableSchema.fromBean(BalanceDynamoDB::class.java)
    )

    override suspend fun accountCurrentBalance(
        accountId: UUID,
    ): CurrentBalanceEntity = CurrentBalanceEntity(
        accountId = accountId,
        currentBalance = accountBalance(accountId),
        dailyTransacted = transactionsTotal(accountId),
    )

    private fun accountBalance(accountId: UUID): BigDecimal = balanceTable.getItem(
        Key.builder()
            .partitionValue(accountId.toString())
            .build()
    )?.amount ?: BigDecimal.ZERO

    private fun transactionsTotal(accountId: UUID): BigDecimal {
        val range = currentDayRange()
        val queryRequest = queryRequest(accountId, range)
        return transactionTable.index("AccountIdIndex")
            .query(queryRequest)
            .stream()
            .map { it.items() }
            .toList()
            .flatten()
            .map { it.amount }
            .fold(BigDecimal.ZERO) { acc, amount -> acc.add(amount) }
    }

    private fun queryRequest(
        accountId: UUID,
        range: Pair<String, String>,
    ) = QueryEnhancedRequest.builder()
        .queryConditional(queryCondition(accountId, range))
        .build()


    private fun queryCondition(
        accountId: UUID,
        range: Pair<String, String>,
    ): QueryConditional = QueryConditional.sortBetween(
        keyCondition(accountId, range.first),
        keyCondition(accountId, range.second)
    )

    private fun keyCondition(
        accountId: UUID,
        timestamp: String,
    ): Key = Key.builder()
        .partitionValue(accountId.toString())
        .sortValue(timestamp)
        .build()

    private fun currentDayRange(): Pair<String, String> = LocalDate.now().let {
        Pair(
            formatter.format(it.atStartOfDay(ZoneOffset.UTC)),
            formatter.format(it.plusDays(1).atStartOfDay(ZoneOffset.UTC).minusNanos(1))
        )
    }

}
