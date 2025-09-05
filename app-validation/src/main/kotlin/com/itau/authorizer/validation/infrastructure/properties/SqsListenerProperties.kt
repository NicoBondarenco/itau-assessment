package com.itau.authorizer.validation.infrastructure.properties

import io.awspring.cloud.sqs.listener.BackPressureMode
import io.awspring.cloud.sqs.listener.acknowledgement.handler.AcknowledgementMode
import java.time.Duration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "spring.cloud.aws.sqs.listener")
data class SqsListenerProperties @ConstructorBinding constructor(
    val maxConcurrentMessages: Int,
    val maxMessagesPerPoll: Int,
    val pollTimeoutSeconds: Long,
    val messageVisibilitySeconds: Long,
    val acknowledgementMode: AcknowledgementMode,
    val acknowledgementIntervalSeconds: Long,
    val acknowledgementThreshold: Int,
    val backPressureMode: BackPressureMode,
) {

    val pollTimeout: Duration
        get() = Duration.ofSeconds(pollTimeoutSeconds)
    val messageVisibility: Duration
        get() = Duration.ofSeconds(messageVisibilitySeconds)
    val acknowledgementInterval: Duration
        get() = Duration.ofSeconds(acknowledgementIntervalSeconds)

}
