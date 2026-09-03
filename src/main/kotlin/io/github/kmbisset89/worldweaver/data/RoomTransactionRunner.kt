package io.github.kmbisset89.worldweaver.data

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import io.github.kmbisset89.worldweaver.domain.TransactionRunner

internal class RoomTransactionRunner(
    private val database: WorldWeaverDatabase,
) : TransactionRunner {
    override suspend fun <T> run(block: suspend () -> T): T {
        return database.useWriterConnection { transactor ->
            transactor.immediateTransaction {
                block()
            }
        }
    }
}
