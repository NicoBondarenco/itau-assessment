package com.itau.authorizer.authorization.application.adapter.out.dynamodb

import com.itau.authorizer.authorization.domain.port.out.CurrentBalanceRetrieverOut
import com.itau.authorizer.common.application.mapper.accountTransactions
import com.itau.authorizer.common.application.mapper.balanceTable
import com.itau.authorizer.common.application.mapper.one
import com.itau.authorizer.common.application.mapper.transactionTable
import com.itau.authorizer.common.domain.exception.BalanceNotFoundException
import com.itau.authorizer.common.domain.model.entity.CurrentBalanceEntity
import com.itau.authorizer.common.util.extension.atEndOfDay
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import java.util.UUID
import org.springframework.stereotype.Repository
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient

@Repository
class CurrentBalanceRetrieverDynamoDB(
    private val client: DynamoDbEnhancedClient,
) : CurrentBalanceRetrieverOut {


    override suspend fun accountCurrentBalance(
        accountId: UUID,
    ): CurrentBalanceEntity = CurrentBalanceEntity(
        accountId = accountId,
        currentBalance = accountBalance(accountId),
        dailyTransacted = transactionsTotal(accountId),
    )

    private fun accountBalance(
        accountId: UUID
    ): BigDecimal = client.balanceTable.one(accountId)?.amount ?: throw BalanceNotFoundException(accountId)

    private fun transactionsTotal(accountId: UUID): BigDecimal = client.transactionTable
        .accountTransactions(accountId, currentDayRange())
        .map { it.amount }
        .fold(BigDecimal.ZERO) { acc, amount -> acc.add(amount) }

    private fun currentDayRange(): Pair<ZonedDateTime, ZonedDateTime> = LocalDate.now().let {
        Pair(
            it.atStartOfDay(UTC),
            it.atEndOfDay(UTC)
        )
    }

}
