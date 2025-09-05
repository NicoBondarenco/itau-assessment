package com.itau.authorizer.web.application.adapter.out.dynamodb

import com.itau.authorizer.common.application.mapper.accountTable
import com.itau.authorizer.common.application.mapper.accountTransactions
import com.itau.authorizer.common.application.mapper.balanceTable
import com.itau.authorizer.common.application.mapper.one
import com.itau.authorizer.common.application.mapper.toAccountEntity
import com.itau.authorizer.common.application.mapper.toBalanceEntity
import com.itau.authorizer.common.application.mapper.toTransactionEntity
import com.itau.authorizer.common.application.mapper.transactionTable
import com.itau.authorizer.common.application.model.dynamodb.AccountDynamoDB
import com.itau.authorizer.common.application.model.dynamodb.BalanceDynamoDB
import com.itau.authorizer.common.application.model.dynamodb.TransactionDynamoDB
import com.itau.authorizer.common.domain.exception.AccountNotFoundException
import com.itau.authorizer.common.domain.exception.BalanceNotFoundException
import com.itau.authorizer.common.domain.model.entity.AccountDataEntity
import com.itau.authorizer.web.domain.port.out.AccountDataRetriever
import io.awspring.cloud.dynamodb.DynamoDbTemplate
import java.time.LocalDate
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import java.util.UUID
import kotlin.reflect.KClass
import org.springframework.stereotype.Repository
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient

@Repository
class AccountDataRetrieverDynamoDB(
    private val client: DynamoDbEnhancedClient,
    private val template: DynamoDbTemplate,
) : AccountDataRetriever {

    override suspend fun one(
        accountId: UUID
    ): AccountDataEntity = client.let {
        client
        val account = client.accountTable.one(accountId)?.toAccountEntity()
            ?: throw AccountNotFoundException(accountId)
        val balance = client.balanceTable.one(accountId)?.toBalanceEntity()
            ?: throw BalanceNotFoundException(accountId)
        val transactions = client.transactionTable.accountTransactions(accountId, yearRange())
            .map { it.toTransactionEntity() }
        AccountDataEntity(
            account = account,
            balance = balance,
            transactions = transactions
        )
    }

    override suspend fun all(): List<AccountDataEntity> {
        val accounts = template.all(AccountDynamoDB::class) { it.toAccountEntity() }
        val balances = template.all(BalanceDynamoDB::class) { it.toBalanceEntity() }
        val transactions = template.all(TransactionDynamoDB::class) { it.toTransactionEntity() }
        return accounts.map {
            AccountDataEntity(
                account = it,
                balance = balances.firstOrNull { b -> b.accountId == it.accountId } ?: throw BalanceNotFoundException(it.accountId),
                transactions = transactions.filter { t -> t.accountId == it.accountId }
            )
        }
    }

    private inline fun <T : Any, K : Any> DynamoDbTemplate.all(
        clazz: KClass<T>,
        crossinline mapper: (T) -> K
    ): List<K> = scanAll(clazz.java)
        .stream()
        .map { page -> page.items().map { mapper(it) } }
        .toList()
        .flatten()

    private fun yearRange(): Pair<ZonedDateTime, ZonedDateTime> = LocalDate.now()
        .withDayOfYear(1)
        .let {
            Pair(
                it.atStartOfDay(UTC),
                it.atStartOfDay(UTC).plusYears(1).minusNanos(1)
            )
        }

}
