package com.mirabilis.data.auth.network

import javax.inject.Qualifier

/**
 * Marks the **anti-loop** networking instances used only for `POST /auth/refresh` — a separate
 * OkHttp/Retrofit without the [interceptor.AuthInterceptor]/`TokenAuthenticator`, so a 401 on the
 * refresh call itself can never recurse (ADR-0006 §3).
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RefreshClient
