package com.itau.authorizer.web.application.adapter.out.sqs

import com.fasterxml.jackson.databind.ObjectMapper
import com.itau.authorizer.common.domain.model.entity.TransactionEntity
import com.itau.authorizer.web.application.mapper.toTransactionCommandSQS
import com.itau.authorizer.web.domain.port.out.TransactionCommandProducer
import io.awspring.cloud.sqs.operations.SqsTemplate
import java.math.BigDecimal
import java.time.ZonedDateTime
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class TransactionCommandProducerSQS(
    private val sqsTemplate: SqsTemplate,
    @Value("\${spring.cloud.aws.sqs.transaction-queue}")
    private val transactionQueue: String,
    private val objectMapper: ObjectMapper
) : TransactionCommandProducer {

    override suspend fun send(
        entity: TransactionEntity,
        invalidTransactionId: String?,
        invalidAccountId: String?,
        invalidAmount: BigDecimal?,
        invalidType: String?,
    ) {
        val json = objectMapper.writeValueAsString(
            entity.toTransactionCommandSQS(
                invalidTransactionId = invalidTransactionId,
                invalidAccountId = invalidAccountId,
                invalidAmount = invalidAmount,
                invalidType = invalidType,
            )
        )
        sqsTemplate.send {
            it.queue(transactionQueue)
                .payload(json)
                .messageGroupId(entity.accountId.toString())
                .headers(
                    mapOf(
                        "transactionId" to entity.transactionId,
                        "accountId" to entity.accountId,
                        "timestamp" to ZonedDateTime.now().toInstant().toEpochMilli()
                    )
                )
        }
    }

}
