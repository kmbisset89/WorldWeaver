package io.github.kmbisset89.worldweaver.domain

internal interface TransactionRunner {
    suspend fun <T> run(block: suspend () -> T): T
}
