package com.itau.authorizer.common.infrastructure.resolver

import com.itau.authorizer.common.application.model.dynamodb.AccountDynamoDB
import com.itau.authorizer.common.application.model.dynamodb.BalanceDynamoDB
import com.itau.authorizer.common.application.model.dynamodb.TransactionDynamoDB
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AppTableNameResolverTest {

    private val resolver = AppTableNameResolver()


    @Test
    fun `resolve should convert AccountDynamoDB to snake_case table name`() {
        val result = resolver.resolve(AccountDynamoDB::class.java)

        assertEquals("account", result)
    }

    @Test
    fun `resolve should convert BalanceDynamoDB to snake_case table name`() {
        val result = resolver.resolve(BalanceDynamoDB::class.java)

        assertEquals("balance", result)
    }

    @Test
    fun `resolve should convert TransactionDynamoDB to snake_case table name`() {
        val result = resolver.resolve(TransactionDynamoDB::class.java)

        assertEquals("transaction", result)
    }

    @Test
    fun `resolve should handle multiple camelCase words`() {
        class CustomerAccountDynamoDB

        val result = resolver.resolve(CustomerAccountDynamoDB::class.java)

        assertEquals("customer_account", result)
    }

}
