package com.itau.authorizer.validation.infrastructure.interceptor

import io.awspring.cloud.sqs.listener.AsyncAdapterBlockingExecutionFailedException
import io.awspring.cloud.sqs.listener.ListenerExecutionFailedException
import io.awspring.cloud.sqs.listener.interceptor.MessageInterceptor
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.CompletionException
import org.springframework.messaging.Message
import org.springframework.stereotype.Component

@Component
class SqsMessageInterceptor : MessageInterceptor<Any> {

    companion object {
        private val OUTTER_EXCEPTIONS: List<Class<out Throwable>> = listOf(
            CompletionException::class.java,
            AsyncAdapterBlockingExecutionFailedException::class.java,
            ListenerExecutionFailedException::class.java,
        )
    }

    private val logger = KotlinLogging.logger {}

    override fun afterProcessing(message: Message<Any>, throwable: Throwable?) {
        throwable?.apply {
            logger.error(throwable.errorException()) { "Error processing message: ${message.payload}" }
        }
    }

    private fun <T : Throwable> T.errorException(): Throwable = this.takeIf {
        !it.isOutterException()
    } ?: this.cause?.errorException() ?: this

    private fun <T : Throwable> T.isOutterException(): Boolean = this::class.java in OUTTER_EXCEPTIONS

}
