package com.mirabilis.data.auth.datastore

import com.mirabilis.core.result.AppError
import com.mirabilis.core.result.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Base for local data sources (ADR-0003/0005). Converts any storage throwable into a typed
 * [AppError] (default [AppError.DataStore]); never crashes.
 */
abstract class SafeLocalDataSource(private val ioDispatcher: CoroutineDispatcher) {

    protected suspend fun <T> safeCall(
        error: AppError = AppError.DataStore,
        block: suspend () -> T,
    ): Result<T> = withContext(ioDispatcher) {
        try {
            Result.Success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Result.Error(error)
        }
    }
}
