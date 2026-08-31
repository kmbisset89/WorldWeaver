package net.tactware.worldweaver.domain

internal interface TransactionRunner {
    suspend fun <T> run(block: suspend () -> T): T
}
