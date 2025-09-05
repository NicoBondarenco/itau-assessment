package com.itau.authorizer.validation.application.extension

import java.util.UUID
import org.springframework.messaging.Message

const val RECEIVE_COUNT_HEADER = "Sqs_Msa_ApproximateReceiveCount"
const val ACCOUNT_ID_HEADER = "accountId"
const val TRANSACTION_ID_HEADER = "transactionId"

val Message<*>.receiveCount: Int
    get() = this.headers[RECEIVE_COUNT_HEADER]?.toString()?.toInt() ?: 1

val Message<*>.accountId: UUID
    get() = UUID.fromString(this.headers[ACCOUNT_ID_HEADER].toString())
