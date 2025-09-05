package com.itau.authorizer.web.application.adapter.`in`.dynamodb

import com.itau.authorizer.common.application.model.dynamodb.AccountDynamoDB
import com.itau.authorizer.common.application.model.dynamodb.BalanceDynamoDB
import com.itau.authorizer.common.application.model.dynamodb.TransactionDynamoDB
import com.itau.authorizer.common.application.mapper.toAccountDynamoDB
import com.itau.authorizer.common.application.mapper.toBalanceDynamoDB
import com.itau.authorizer.common.application.mapper.toTransactionDynamoDB
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
            dynamoDbTemplate.save<AccountDynamoDB>(data.account.toAccountDynamoDB())
            dynamoDbTemplate.save<BalanceDynamoDB>(data.balance.toBalanceDynamoDB())
            data.transactions.forEach { transaction ->
                dynamoDbTemplate.save<TransactionDynamoDB>(transaction.toTransactionDynamoDB())
            }
        }
    }

}
