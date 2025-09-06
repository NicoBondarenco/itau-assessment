package com.itau.authorizer.web.application.adapter.`in`.dynamodb

import com.itau.authorizer.common.application.mapper.accountTable
import com.itau.authorizer.common.application.mapper.balanceTable
import com.itau.authorizer.common.application.mapper.toAccountDynamoDB
import com.itau.authorizer.common.application.mapper.toBalanceDynamoDB
import com.itau.authorizer.common.application.mapper.toTransactionDynamoDB
import com.itau.authorizer.common.application.mapper.transactionTable
import com.itau.authorizer.common.application.model.dynamodb.AccountDynamoDB
import com.itau.authorizer.common.application.model.dynamodb.BalanceDynamoDB
import com.itau.authorizer.common.application.model.dynamodb.TransactionDynamoDB
import com.itau.authorizer.common.domain.model.entity.AccountDataEntity
import com.itau.authorizer.web.domain.port.`in`.AccountBatchSaverIn
import io.awspring.cloud.dynamodb.DynamoDbTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Repository
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.MappedTableResource
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch

@Repository
class AccountBatchSaverDynamoDB(
    private val dynamoDbTemplate: DynamoDbTemplate,
    private val client: DynamoDbEnhancedClient,
) : AccountBatchSaverIn {

    private fun <T : Any> List<T>.saveBatch(
        clazz: Class<T>,
        table: MappedTableResource<T>,
    ) {
        this.chunked(25).map { chunk ->
            val write = WriteBatch.builder(clazz)
                .mappedTableResource(table)
                .apply {
                    chunk.forEach { addPutItem(it) }
                }.build()

            val batchRequest = BatchWriteItemEnhancedRequest.builder()
                .writeBatches(write)
                .build()

            client.batchWriteItem(batchRequest)
        }
    }

    override suspend fun saveAll(accounts: List<AccountDataEntity>) {
        withContext(Dispatchers.IO) {

            val accountList = mutableListOf<AccountDynamoDB>()
            val balanceList = mutableListOf<BalanceDynamoDB>()
            val transactionList = mutableListOf<TransactionDynamoDB>()

            accounts.forEach {
                accountList.add(it.account.toAccountDynamoDB())
                balanceList.add(it.balance.toBalanceDynamoDB())
                transactionList.addAll(it.transactions.map { transaction -> transaction.toTransactionDynamoDB() })
            }

            accountList.saveBatch(AccountDynamoDB::class.java, client.accountTable)
            balanceList.saveBatch(BalanceDynamoDB::class.java, client.balanceTable)
            transactionList.saveBatch(TransactionDynamoDB::class.java, client.transactionTable)
        }
    }

}
