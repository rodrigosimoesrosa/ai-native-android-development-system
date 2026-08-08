package com.mirabilis.data.auth.session

import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory cache of the current tokens (ADR-0006 §4). OkHttp `Interceptor`/`Authenticator` run
 * synchronously and read from here instead of doing a `suspend` DataStore read per request.
 * DataStore remains the durable, encrypted source of truth; this cache is kept in sync by
 * [com.mirabilis.data.auth.datastore.SessionLocalDataSource] on every write, and primed at startup.
 */
@Singleton
class SessionHolder @Inject constructor() {

    @Volatile
    var accessToken: String? = null
        private set

    @Volatile
    var refreshToken: String? = null
        private set

    fun update(access: String?, refresh: String?) {
        accessToken = access
        refreshToken = refresh
    }

    fun clear() {
        accessToken = null
        refreshToken = null
    }
}
