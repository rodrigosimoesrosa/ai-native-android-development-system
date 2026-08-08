package com.mirabilis.data.auth.network

import com.mirabilis.core.result.AppError
import com.mirabilis.core.result.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * Base for remote data sources (ADR-0003/0006). Wraps an IO call, converting any throwable into a
 * typed [AppError] — a data source never leaks a raw exception. Runs on the injected IO dispatcher.
 */
abstract class SafeRemoteDataSource(private val ioDispatcher: CoroutineDispatcher) {

    protected suspend fun <T> safeCall(block: suspend () -> T): Result<T> =
        withContext(ioDispatcher) {
            try {
                Result.Success(block())
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpException) {
                Result.Error(AppError.Server(e.code(), e.response()?.errorBody()?.string()))
            } catch (e: IOException) {
                Result.Error(AppError.Network)
            } catch (e: Throwable) {
                Result.Error(AppError.Unknown(e))
            }
        }
}
