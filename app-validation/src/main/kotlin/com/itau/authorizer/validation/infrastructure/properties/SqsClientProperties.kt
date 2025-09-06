package com.itau.authorizer.validation.infrastructure.properties

import java.time.Duration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "spring.cloud.aws.sqs.client")
data class SqsClientProperties @ConstructorBinding constructor(
    var maxConcurrency: Int,
    var maxPendingConnectionAcquires: Int,
    var connectionAcquisitionTimeout: Long,
    var connectionTimeout: Long,
    var connectionTimeToLive: Long,
) {

    val connectionAcquisitionTimeoutDuration: Duration
        get() = Duration.ofSeconds(connectionAcquisitionTimeout)

    val connectionTimeoutDuration: Duration
        get() = Duration.ofSeconds(connectionTimeout)

    val connectionTimeToLiveDuration: Duration
        get() = Duration.ofSeconds(connectionTimeToLive)

}
