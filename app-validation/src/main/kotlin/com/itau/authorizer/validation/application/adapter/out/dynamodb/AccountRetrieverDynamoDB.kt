package com.itau.authorizer.validation.application.adapter.out.dynamodb

import com.itau.authorizer.common.application.mapper.accountTable
import com.itau.authorizer.common.application.mapper.one
import com.itau.authorizer.common.application.mapper.toAccountEntity
import com.itau.authorizer.common.domain.exception.AccountNotFoundException
import com.itau.authorizer.common.domain.model.entity.AccountEntity
import com.itau.authorizer.validation.domain.port.out.AccountRetrieverOut
import java.util.UUID
import org.springframework.stereotype.Repository
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient

@Repository
class AccountRetrieverDynamoDB(
    private val client: DynamoDbEnhancedClient,
) : AccountRetrieverOut {

    override suspend fun retrieveAccount(
        accountId: UUID,
    ): AccountEntity = (client.accountTable.one(accountId)
        ?: throw AccountNotFoundException(accountId))
        .toAccountEntity()


}
