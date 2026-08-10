package com.mirabilis.data.auth.network.interceptor

import com.mirabilis.core.result.Result
import com.mirabilis.data.auth.datastore.ISessionLocalDataSource
import com.mirabilis.data.auth.network.TokenRefresher
import com.mirabilis.data.auth.session.SessionHolder
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

/**
 * The idiomatic 401 handler (ADR-0006 §2–§4). On a `401` it refreshes the access token and returns
 * the original request rebuilt with the new token (OkHttp auto-retries), or `null` to give up.
 *
 * Correctness guarantees:
 *  - **Single-flight:** a [Mutex] serialises refreshes; concurrent 401s trigger exactly one refresh.
 *    Before refreshing, the failed request's token is compared to the currently stored token — if
 *    another thread already refreshed, we just retry with the current token (no second refresh).
 *  - **Anti-loop:** the refresh call uses [TokenRefresher] (a separate client without this
 *    authenticator), and retries are bounded via [Response.priorResponse] ([MAX_ATTEMPTS]).
 *  - **Refresh failure ⇒ logout:** the session is cleared (FR-010); auth-state then flips to false.
 *
 * `authenticate` runs synchronously on an OkHttp thread, so [runBlocking] here is appropriate (it
 * only fires on a 401, not on every request).
 */
class TokenAuthenticator @Inject constructor(
    private val sessionHolder: SessionHolder,
    private val tokenRefresher: TokenRefresher,
    private val sessionLocal: ISessionLocalDataSource,
) : Authenticator {

    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (priorResponseCount(response) >= MAX_ATTEMPTS) return null

        val failedToken = response.request.header(AUTHORIZATION)
            ?.substringAfter("Bearer ")?.trim()

        return runBlocking {
            mutex.withLock {
                val current = sessionHolder.accessToken
                // Another thread already refreshed → retry with the current token, no second refresh.
                if (!current.isNullOrBlank() && current != failedToken) {
                    return@withLock response.request.withBearer(current)
                }

                val refreshToken = sessionHolder.refreshToken
                if (refreshToken.isNullOrBlank()) {
                    sessionLocal.clear()
                    return@withLock null
                }

                when (val result = tokenRefresher.refresh(refreshToken)) {
                    is Result.Success -> {
                        sessionLocal.updateTokens(result.data) // persists (DataStore + holder)
                        response.request.withBearer(result.data.accessToken)
                    }
                    is Result.Error -> {
                        sessionLocal.clear() // non-renewable → logout (FR-010)
                        null
                    }
                }
            }
        }
    }

    private fun Request.withBearer(token: String): Request =
        newBuilder().header(AUTHORIZATION, "Bearer $token").build()

    private fun priorResponseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

    private companion object {
        const val AUTHORIZATION = "Authorization"
        const val MAX_ATTEMPTS = 2
    }
}
