package com.itau.authorizer.validation.domain.exception

import com.itau.authorizer.common.domain.model.value.TransactionType

class InvalidTransactionTypeException(
    transactionType: TransactionType
): RuntimeException("Invalid transaction type $transactionType")
