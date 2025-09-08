package com.itau.authorizer.authorization.domain.usecase

import com.itau.authorizer.authorization.application.adapter.out.grpc.TestTransactionExecutorRPC
import com.itau.authorizer.authorization.application.adapter.out.kafka.TransactionExecutedProducerKafka.Companion.TRANSACTION_EXECUTED_ACCOUNT
import com.itau.authorizer.authorization.application.adapter.out.kafka.TransactionExecutedProducerKafka.Companion.TRANSACTION_EXECUTED_KEY
import com.itau.authorizer.authorization.application.model.kafka.TransactionExecutedKafka
import com.itau.authorizer.authorization.util.RandomGenerator.randomBigDecimal
import com.itau.authorizer.common.application.model.dynamodb.BalanceDynamoDB
import com.itau.authorizer.common.application.model.dynamodb.TransactionDynamoDB
import com.itau.authorizer.common.util.extension.toIsoFormat
import io.grpc.Status
import io.grpc.StatusException
import java.util.UUID.randomUUID
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.binder.test.OutputDestination
import org.springframework.cloud.stream.schema.registry.avro.AvroSchemaMessageConverter

class ExecuteTransactionTest : BaseContextTest() {

    @Autowired
    private lateinit var outputDestination: OutputDestination

    @Autowired
    private lateinit var rpc: TestTransactionExecutorRPC

    @Autowired
    private lateinit var avroConverter: AvroSchemaMessageConverter

    @BeforeEach
    fun beforeEach() {
        client.clear()
        outputDestination.clear()
    }

    @Test
    fun `execute transaction successfully`() = runTest(StandardTestDispatcher()) {
        val accountId = randomUUID()
        client.generateBalance(
            accountId = accountId,
            amount = randomBigDecimal(1000.0, 2000.0),
        )
        val request = generateTransactionEntity(
            accountId = accountId,
            amount = randomBigDecimal(100.0, 200.0),
        )
        rpc.executeTransaction(request)

        val message = outputDestination.await()
        assertNotNull(message)
        assertEquals(request.transactionId, message!!.headers[TRANSACTION_EXECUTED_KEY])
        assertEquals(request.accountId, message.headers[TRANSACTION_EXECUTED_ACCOUNT])

        val kafka = avroConverter.fromMessage(message, TransactionExecutedKafka::class.java) as TransactionExecutedKafka
        assertEquals(request.transactionId, kafka.transactionId)
        assertEquals(request.accountId, kafka.accountId)
        assertEquals(request.amount, kafka.amount)
        assertEquals(request.type.name, kafka.type)
        assertEquals(request.timestamp.toIsoFormat(), kafka.timestamp)

        val balance = client.get<BalanceDynamoDB>(accountId)
        assertNotNull(balance)
        assertEquals(balance!!.amount, kafka.currentBalance)

        val transaction = client.get<TransactionDynamoDB>(request.transactionId)
        assertNotNull(transaction)
        assertEquals(transaction!!.accountId, request.accountId)
        assertEquals(transaction.amount, request.amount)
        assertEquals(transaction.type, request.type)
        assertEquals(transaction.timestamp, request.timestamp)
    }

    @Test
    fun `execute transaction balance not found`() = runTest(StandardTestDispatcher()) {
        val request = generateTransactionEntity()
        val exception = assertThrows<StatusException> {
            rpc.executeTransaction(request)
        }
        assertEquals(Status.NOT_FOUND.code, exception.status.code)
        assertEquals("Account ${request.accountId} has no balance", exception.status.description)

        val message = outputDestination.await()
        assertNull(message)

        val balance = client.get<BalanceDynamoDB>(request.accountId)
        assertNull(balance)

        val transaction = client.get<TransactionDynamoDB>(request.transactionId)
        assertNull(transaction)
    }

}
