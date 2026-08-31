package net.tactware.worldweaver.data

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import net.tactware.worldweaver.domain.TransactionRunner

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
