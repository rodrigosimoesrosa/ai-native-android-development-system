package com.mirabilis.app

import android.app.Application
import com.mirabilis.data.auth.datastore.ISessionLocalDataSource
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Composition root (ADR-0004). Primes the in-memory token cache from encrypted storage at cold
 *  start so the authed client has the bearer available after a process restart (ADR-0006 §4). */
@HiltAndroidApp
class MirabilisApp : Application() {

    @Inject lateinit var sessionLocal: ISessionLocalDataSource

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        appScope.launch { sessionLocal.prime() }
    }
}
