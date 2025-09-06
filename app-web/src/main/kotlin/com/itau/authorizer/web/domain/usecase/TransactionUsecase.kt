package com.itau.authorizer.web.domain.usecase

import com.itau.authorizer.common.domain.model.entity.AccountEntity
import com.itau.authorizer.common.domain.model.entity.TransactionEntity
import com.itau.authorizer.common.domain.model.value.TransactionType
import com.itau.authorizer.web.domain.port.out.AccountListRetrieverOut
import com.itau.authorizer.web.domain.port.out.TransactionCommandProducerOut
import java.math.BigDecimal
import java.math.BigDecimal.ZERO
import java.time.ZonedDateTime
import java.util.UUID
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class TransactionUsecase(
    private val transactionCommandProducer: TransactionCommandProducerOut,
    private val accountListRetriever: AccountListRetrieverOut
) {
    private var accounts: List<AccountEntity> = listOf()

    suspend fun produceCommands(quantity: Int) = withContext(IO) {
        accounts = accountListRetriever.allAccounts()
        (1..quantity).map {
            async {
                transactionCommandProducer.send(generateTransaction())
            }
        }.awaitAll()
    }

    suspend fun produceErrorCommands(quantity: Int) = withContext(IO) {
        accounts = accountListRetriever.allAccounts()
        (1..quantity).map {
            async {
                listOf(
                    ::generateTransactionWithTransactionIdError,
                    ::generateTransactionWithAccountIdError,
                    ::generateTransactionWithAmountError,
                    ::generateTransactionWithTypeError,
                ).random().invoke()
            }
        }.awaitAll()
    }

    private suspend fun generateTransactionWithTransactionIdError() = transactionCommandProducer.send(
        entity = generateTransaction(),
        invalidTransactionId = "qwerty",
    )

    private suspend fun generateTransactionWithAccountIdError() = transactionCommandProducer.send(
        entity = generateTransaction(),
        invalidAccountId = "qwerty",
    )

    private suspend fun generateTransactionWithAmountError() = generateTransactionWithAmountError(ZERO)

    private suspend fun generateTransactionWithAmountError(
        amount: BigDecimal,
    ) = transactionCommandProducer.send(
        entity = generateTransaction(),
        invalidAmount = amount,
    )

    private suspend fun generateTransactionWithTypeError() = transactionCommandProducer.send(
        entity = generateTransaction(),
        invalidType = "QWERTY",
    )

    private suspend fun generateTransaction(
        transactionId: UUID = UUID.randomUUID(),
        accountId: UUID = accounts.random().accountId,
        amount: BigDecimal = BigDecimal.valueOf(Random.nextDouble(100.0, 300.0)),
        type: TransactionType = TransactionType.entries.random(),
        timestamp: ZonedDateTime = ZonedDateTime.now()
    ) = TransactionEntity(
        transactionId = transactionId,
        accountId = accountId,
        amount = amount,
        type = type,
        timestamp = timestamp,
    )

}
