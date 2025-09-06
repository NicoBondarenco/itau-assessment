package com.itau.authorizer.authorization.application.adapter.out.dynamodb

import com.itau.authorizer.authorization.domain.port.out.BalanceRetrieverOut
import com.itau.authorizer.common.application.mapper.balanceTable
import com.itau.authorizer.common.application.mapper.one
import com.itau.authorizer.common.application.mapper.toBalanceEntity
import com.itau.authorizer.common.domain.exception.BalanceNotFoundException
import com.itau.authorizer.common.domain.model.entity.BalanceEntity
import java.util.UUID
import org.springframework.stereotype.Repository
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient

@Repository
class BalanceRetrieverDynamoDB(
    private val client: DynamoDbEnhancedClient,
) : BalanceRetrieverOut {

    override suspend fun accountBalance(
        accountId: UUID
    ): BalanceEntity = client.balanceTable.one(accountId)
        ?.toBalanceEntity()
        ?: throw BalanceNotFoundException(accountId)

}
