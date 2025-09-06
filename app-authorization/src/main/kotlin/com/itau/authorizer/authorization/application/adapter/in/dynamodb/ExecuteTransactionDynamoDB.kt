package com.itau.authorizer.authorization.application.adapter.`in`.dynamodb

import com.itau.authorizer.authorization.domain.port.`in`.ExecuteTransactionIn
import com.itau.authorizer.common.application.mapper.balanceTable
import com.itau.authorizer.common.application.mapper.toBalanceDynamoDB
import com.itau.authorizer.common.application.mapper.toTransactionDynamoDB
import com.itau.authorizer.common.application.mapper.transactionTable
import com.itau.authorizer.common.domain.model.entity.AccountTransactionEntity
import org.springframework.stereotype.Repository
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient

@Repository
class ExecuteTransactionDynamoDB(
    private val client: DynamoDbEnhancedClient,
) : ExecuteTransactionIn {

    override suspend fun executeTransaction(
        entity: AccountTransactionEntity
    ): AccountTransactionEntity = entity.apply {
        client.transactWriteItems {
            it.addPutItem(client.transactionTable, entity.toTransactionDynamoDB())
            it.addUpdateItem(client.balanceTable, entity.toBalanceDynamoDB())
        }
    }

}
