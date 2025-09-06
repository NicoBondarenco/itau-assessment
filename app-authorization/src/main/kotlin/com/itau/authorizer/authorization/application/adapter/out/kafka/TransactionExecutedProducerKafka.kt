package com.itau.authorizer.authorization.application.adapter.out.kafka

import com.itau.authorizer.authorization.application.mapper.toTransactionExecutedKafka
import com.itau.authorizer.authorization.domain.port.out.TransactionExecutedProducerOut
import com.itau.authorizer.common.domain.model.entity.AccountTransactionEntity
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component

@Component
class TransactionExecutedProducerKafka(
    private val streamBridge: StreamBridge
) : TransactionExecutedProducerOut {

    companion object {
        const val TRANSACTION_EXECUTED_OUTPUT = "transactionExecutedEvent-out-0"
        const val TRANSACTION_EXECUTED_KEY = "messageKey"
        const val TRANSACTION_EXECUTED_ACCOUNT = "accountId"
    }

    override suspend fun produce(entity: AccountTransactionEntity) {
        withContext(IO) {
            streamBridge.send(
                TRANSACTION_EXECUTED_OUTPUT,
                MessageBuilder
                    .withPayload(entity.toTransactionExecutedKafka())
                    .setHeader(TRANSACTION_EXECUTED_KEY, entity.transactionId)
                    .setHeader(TRANSACTION_EXECUTED_ACCOUNT, entity.accountId)
                    .build()
            )
        }
    }

}
