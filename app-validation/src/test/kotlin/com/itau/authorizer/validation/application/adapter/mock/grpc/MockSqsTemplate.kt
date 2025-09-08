package com.itau.authorizer.validation.application.adapter.mock.grpc

import io.awspring.cloud.sqs.operations.SendResult
import io.awspring.cloud.sqs.operations.SqsSendOptions
import io.awspring.cloud.sqs.operations.SqsTemplate
import io.mockk.every
import io.mockk.mockk
import java.util.UUID.randomUUID
import java.util.function.Consumer
import org.springframework.messaging.Message
import org.springframework.messaging.support.GenericMessage

val dlqMessages = mutableListOf<Message<String>>()

fun mockSqsTemplate() = mockk<SqsTemplate>().apply {
    every { send(any<Consumer<SqsSendOptions<String>>>()) } answers {
        val consumer = firstArg<Consumer<SqsSendOptions<String>>>()
        MockSqsSendOptions().let {
            consumer.accept(it)
            it.sendResult().takeIf { result ->
                result.message.payload != "exception"
            }?.apply {
                dlqMessages.add(it.sendResult().message)
            } ?: throw Exception("exception")
        }
    }
}

private class MockSqsSendOptions : SqsSendOptions<String> {

    var queue: String? = null
    var payload: String? = null
    var headers: MutableMap<String, Any?> = mutableMapOf()
    var delaySeconds: Int? = null
    var messageGroupId: String? = null
    var messageDeduplicationId: String? = null

    fun sendResult() = SendResult(
        randomUUID(),
        this.queue ?: "",
        GenericMessage(payload ?: ""),
        mutableMapOf<String, Any>()
    )

    override fun queue(queue: String): SqsSendOptions<String> = this.apply {
        this.queue = queue
    }

    override fun payload(payload: String): SqsSendOptions<String> = this.apply {
        this.payload = payload
    }

    override fun header(headerName: String, headerValue: Any): SqsSendOptions<String> = this.apply {
        this.headers[headerName] = headerValue
    }

    override fun headers(headers: Map<String, Any?>): SqsSendOptions<String> = this.apply {
        this.headers = headers.toMutableMap()
    }

    override fun delaySeconds(delaySeconds: Int): SqsSendOptions<String> = this.apply {
        this.delaySeconds = delaySeconds
    }

    override fun messageGroupId(messageGroupId: String): SqsSendOptions<String> = this.apply {
        this.messageGroupId = messageGroupId
    }

    override fun messageDeduplicationId(messageDeduplicationId: String): SqsSendOptions<String> = this.apply {
        this.messageDeduplicationId = messageDeduplicationId
    }
}
