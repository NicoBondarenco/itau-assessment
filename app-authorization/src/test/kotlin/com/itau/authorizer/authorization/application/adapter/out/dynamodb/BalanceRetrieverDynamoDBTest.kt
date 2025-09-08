package com.itau.authorizer.authorization.application.adapter.out.dynamodb

import com.itau.authorizer.authorization.application.adapter.mock.dynamodb.MockDynamoDbEnhancedClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BalanceRetrieverDynamoDBTest {

    private val client = MockDynamoDbEnhancedClient.INSTANCE
    private val repository = BalanceRetrieverDynamoDB(client)

//    @BeforeEach
    fun beforeEach() {
        client.clear()
    }

//    @Test
    fun `test`(){

    }

}
