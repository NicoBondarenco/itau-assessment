package com.itau.authorizer.web.domain.port.out

import com.itau.authorizer.common.domain.model.entity.TransactionEntity
import java.math.BigDecimal

interface TransactionCommandProducer {

    suspend fun send(
        entity: TransactionEntity,
        invalidTransactionId: String? = null,
        invalidAccountId: String? = null,
        invalidAmount: BigDecimal? = null,
        invalidType: String? = null,
    )

}
