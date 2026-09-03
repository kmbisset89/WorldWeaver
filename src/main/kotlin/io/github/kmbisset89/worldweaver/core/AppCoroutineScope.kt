package io.github.kmbisset89.worldweaver.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Application-scoped coroutine scope shared by ViewModels.
 * Uses a supervisor job so child failures do not cancel siblings.
 */
internal class AppCoroutineScope {
    private val job = SupervisorJob()
    val scope: CoroutineScope = CoroutineScope(job + Dispatchers.Default)

    fun cancel() {
        job.cancel()
    }
}
