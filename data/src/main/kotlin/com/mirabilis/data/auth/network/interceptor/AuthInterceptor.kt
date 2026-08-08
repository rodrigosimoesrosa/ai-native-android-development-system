package com.mirabilis.data.auth.network.interceptor

import com.mirabilis.data.auth.network.AuthInterceptorHeaders
import com.mirabilis.data.auth.session.SessionHolder
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Attaches `Authorization: Bearer <access>` to protected requests (ADR-0006 §2). Requests carrying
 * the [AuthInterceptorHeaders.NO_AUTH] marker (otp request/verify, auth refresh) are public — stripped;
 * no token is attached. Reads the token synchronously from [SessionHolder].
 */
class AuthInterceptor @Inject constructor(
    private val sessionHolder: SessionHolder,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (request.header(AuthInterceptorHeaders.NO_AUTH) != null) {
            val public = request.newBuilder().removeHeader(AuthInterceptorHeaders.NO_AUTH).build()
            return chain.proceed(public)
        }

        val token = sessionHolder.accessToken
        val authed = if (!token.isNullOrBlank()) {
            request.newBuilder().header("Authorization", "Bearer $token").build()
        } else {
            request
        }
        return chain.proceed(authed)
    }
}
