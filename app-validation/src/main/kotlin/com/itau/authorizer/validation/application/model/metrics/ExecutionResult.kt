package com.itau.authorizer.validation.application.model.metrics

import kotlin.reflect.KClass

class ExecutionResult private constructor(
    val isSuccess: Boolean,
    val errorType: KClass<*>? = null,
    val errorException: Exception? = null,
) {

    companion object {

        fun success() = ExecutionResult(true)

        fun error(
            errorType: KClass<*>,
            errorException: Exception?,
        ) = ExecutionResult(false, errorType, errorException)

    }

}
