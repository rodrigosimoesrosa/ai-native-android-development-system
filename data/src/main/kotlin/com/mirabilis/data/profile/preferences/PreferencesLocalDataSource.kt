package com.mirabilis.data.profile.preferences

import androidx.datastore.core.DataStore
import com.mirabilis.core.dispatcher.Dispatcher
import com.mirabilis.core.dispatcher.DispatcherType
import com.mirabilis.core.result.Result
import com.mirabilis.data.auth.datastore.SafeLocalDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Local, encrypted preferences store (ADR-0005). Reuses [SafeLocalDataSource]'s error boundary. */
interface IPreferencesLocalDataSource {
    fun preferences(): Flow<PreferencesProto>
    suspend fun update(transform: (PreferencesProto) -> PreferencesProto): Result<Unit>
}

class PreferencesLocalDataSource @Inject constructor(
    private val store: DataStore<PreferencesProto>,
    @Dispatcher(DispatcherType.IO) io: CoroutineDispatcher,
) : SafeLocalDataSource(io), IPreferencesLocalDataSource {

    override fun preferences(): Flow<PreferencesProto> = store.data

    override suspend fun update(transform: (PreferencesProto) -> PreferencesProto): Result<Unit> =
        safeCall { store.updateData { transform(it) } }
}
