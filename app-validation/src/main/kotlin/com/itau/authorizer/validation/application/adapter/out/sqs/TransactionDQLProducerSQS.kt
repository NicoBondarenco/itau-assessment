package com.itau.authorizer.validation.application.adapter.out.sqs

import com.fasterxml.jackson.databind.ObjectMapper
import com.itau.authorizer.validation.application.model.sqs.TransactionCommandSQS
import io.awspring.cloud.sqs.operations.SqsTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.Message
import org.springframework.stereotype.Component

@Component
class TransactionDQLProducerSQS(
    private val sqsTemplate: SqsTemplate,
    @Value("\${spring.cloud.aws.sqs.queue.account-transaction-dlq-name}")
    private val dlqQueue: String,
) {

    suspend fun send(message: Message<String>) {
        sqsTemplate.send {
            it.queue(dlqQueue)
                .payload(message.payload)
                .headers(message.headers)
        }
    }

}
