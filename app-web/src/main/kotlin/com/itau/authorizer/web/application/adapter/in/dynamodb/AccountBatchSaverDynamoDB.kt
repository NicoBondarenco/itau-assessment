package com.itau.authorizer.web.application.adapter.`in`.dynamodb

import com.itau.authorizer.web.application.mapper.toAccountDynamoDB
import com.itau.authorizer.web.application.mapper.toBalanceDynamoDB
import com.itau.authorizer.web.application.mapper.toTransactionDynamoDB
import com.itau.authorizer.common.domain.model.entity.AccountDataEntity
import com.itau.authorizer.web.domain.port.`in`.AccountBatchSaver
import io.awspring.cloud.dynamodb.DynamoDbTemplate
import org.springframework.stereotype.Repository

@Repository
class AccountBatchSaverDynamoDB(
    private val dynamoDbTemplate: DynamoDbTemplate,
) : AccountBatchSaver {

    override fun saveAll(accounts: List<AccountDataEntity>) {
        accounts.forEach { data ->
            dynamoDbTemplate.save(data.account.toAccountDynamoDB())
            dynamoDbTemplate.save(data.balance.toBalanceDynamoDB())
            data.transactions.forEach { transaction ->
                dynamoDbTemplate.save(transaction.toTransactionDynamoDB())
            }
        }
    }

}
