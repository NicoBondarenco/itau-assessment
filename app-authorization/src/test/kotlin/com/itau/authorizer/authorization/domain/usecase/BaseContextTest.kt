package com.itau.authorizer.authorization.domain.usecase

import com.itau.authorizer.authorization.application.adapter.mock.dynamodb.MockDynamoDbEnhancedClient
import com.itau.authorizer.authorization.infrastructure.configuration.MockTestConfiguration
import com.itau.authorizer.authorization.util.RandomGenerator.randomBigDecimal
import com.itau.authorizer.authorization.util.RandomGenerator.randomTransactionType
import com.itau.authorizer.authorization.util.RandomGenerator.randomZonedDateTime
import com.itau.authorizer.common.application.model.dynamodb.BalanceDynamoDB
import com.itau.authorizer.common.application.model.dynamodb.TransactionDynamoDB
import com.itau.authorizer.common.domain.model.entity.TransactionEntity
import com.itau.authorizer.common.domain.model.value.TransactionType
import com.itau.authorizer.common.domain.model.value.TransactionType.UNKNOWN
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.UUID
import java.util.UUID.randomUUID
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE
import org.springframework.cloud.stream.binder.test.OutputDestination
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Import
import org.springframework.messaging.Message
import org.springframework.test.context.ActiveProfiles


@SpringBootTest(webEnvironment = NONE)
@ActiveProfiles("test")
@Import(TestChannelBinderConfiguration::class, MockTestConfiguration::class)
abstract class BaseContextTest {

    @Autowired
    protected lateinit var client: MockDynamoDbEnhancedClient

    protected suspend fun OutputDestination.await(): Message<ByteArray>? {
        var message: Message<ByteArray>? = null
        repeat(5) {
            receive(1000, "transaction-executed-event.destination")?.apply { message = this }
        }
        return message
    }

    protected fun generateTransactionEntity(
        transactionId: UUID = randomUUID(),
        accountId: UUID = randomUUID(),
        amount: BigDecimal = randomBigDecimal(),
        type: TransactionType = randomTransactionType(),
        timestamp: ZonedDateTime = randomZonedDateTime(),
    ) = TransactionEntity(
        transactionId = transactionId,
        accountId = accountId,
        amount = amount,
        type = type,
        timestamp = timestamp,
    )

    protected fun MockDynamoDbEnhancedClient.generateBalance(
        accountId: UUID = randomUUID(),
        amount: BigDecimal = randomBigDecimal(),
    ) = generateBalanceDynamoDB(accountId, amount).apply {
        put(this)
    }

    protected fun MockDynamoDbEnhancedClient.generateTransaction(
        transactionId: UUID = randomUUID(),
        accountId: UUID = randomUUID(),
        amount: BigDecimal = randomBigDecimal(),
        type: TransactionType = randomTransactionType(listOf(UNKNOWN)),
        timestamp: ZonedDateTime = randomZonedDateTime(),
    ) = generateTransactionDynamoDB(
        transactionId = transactionId,
        accountId = accountId,
        amount = amount,
        type = type,
        timestamp = timestamp,
    ).apply {
        put(this)
    }

    protected fun generateBalanceDynamoDB(
        accountId: UUID = randomUUID(),
        amount: BigDecimal = randomBigDecimal(),
    ) = BalanceDynamoDB(
        accountId = accountId,
        amount = amount
    )

    protected fun generateTransactionDynamoDB(
        transactionId: UUID = randomUUID(),
        accountId: UUID = randomUUID(),
        amount: BigDecimal = randomBigDecimal(),
        type: TransactionType = randomTransactionType(listOf(UNKNOWN)),
        timestamp: ZonedDateTime = randomZonedDateTime(),
    ) = TransactionDynamoDB(
        transactionId = transactionId,
        accountId = accountId,
        amount = amount,
        type = type,
        timestamp = timestamp,
    )

}
