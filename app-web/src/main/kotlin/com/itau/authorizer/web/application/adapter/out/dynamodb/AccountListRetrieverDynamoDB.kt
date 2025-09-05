package com.itau.authorizer.web.application.adapter.out.dynamodb

import com.itau.authorizer.common.application.mapper.toAccountEntity
import com.itau.authorizer.common.application.model.dynamodb.AccountDynamoDB
import com.itau.authorizer.common.domain.model.entity.AccountEntity
import com.itau.authorizer.web.domain.port.out.AccountListRetriever
import io.awspring.cloud.dynamodb.DynamoDbTemplate
import org.springframework.stereotype.Repository

@Repository
class AccountListRetrieverDynamoDB(
    private val dynamoDbTemplate: DynamoDbTemplate,
): AccountListRetriever {

    override fun allAccounts(): List<AccountEntity> = dynamoDbTemplate
        .scanAll(AccountDynamoDB::class.java)
        .stream()
        .map { p -> p.items().map { it.toAccountEntity() } }
        .toList()
        .flatten()

}
