package com.itau.authorizer.validation.application.adapter.out.sqs

import com.fasterxml.jackson.databind.ObjectMapper
import com.itau.authorizer.validation.application.extension.ACCOUNT_ID_HEADER
import com.itau.authorizer.validation.application.extension.RECEIVE_COUNT_HEADER
import com.itau.authorizer.validation.application.extension.TRANSACTION_ID_HEADER
import com.itau.authorizer.validation.application.model.sqs.TransactionCommandSQS
import com.itau.authorizer.validation.infrastructure.properties.AccountTransactionProperties
import io.awspring.cloud.sqs.operations.SqsTemplate
import org.springframework.stereotype.Component

@Component
class TransactionProducerSQS(
    private val sqsTemplate: SqsTemplate,
//    private val accountTransactionProperties: AccountTransactionProperties,
    private val objectMapper: ObjectMapper,
) {

    fun resend(command: TransactionCommandSQS) {
//        sqsTemplate.send {
//            it.queue(accountTransactionProperties.queueName)
//                .payload(objectMapper.writeValueAsString(command))
//                .headers(
//                    mapOf(
//                        TRANSACTION_ID_HEADER to command.transactionId,
//                        ACCOUNT_ID_HEADER to command.accountId,
//                        RECEIVE_COUNT_HEADER to "1"
//                    )
//                )
//        }
    }

}
