package com.mirabilis.data.profile

import com.mirabilis.core.result.Result
import com.mirabilis.data.profile.preferences.IPreferencesLocalDataSource
import com.mirabilis.data.profile.preferences.PreferencesProto
import com.mirabilis.data.profile.repository.PreferencesRepository
import com.mirabilis.domain.profile.model.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * US3/FR-004: preference writes persist and survive a "reload" (a fresh repository over the same
 * store). Uses an in-memory local data source; the encrypted Proto DataStore is exercised on-device.
 */
class PreferencesRepositoryTest {

    /** Stands in for the Proto DataStore-backed local source, keeping the [PreferencesProto] in memory. */
    private class FakeLocal : IPreferencesLocalDataSource {
        val store = MutableStateFlow(PreferencesProto.getDefaultInstance())
        override fun preferences(): Flow<PreferencesProto> = store
        override suspend fun update(transform: (PreferencesProto) -> PreferencesProto): Result<Unit> {
            store.value = transform(store.value)
            return Result.Success(Unit)
        }
    }

    @Test
    fun `defaults map to System theme and notifications on`() = runTest {
        val prefs = PreferencesRepository(FakeLocal()).observe().first()

        assertEquals(Theme.System, prefs.theme)
        assertTrue(prefs.notificationsEnabled)
    }

    @Test
    fun `theme and notification writes persist across a reload`() = runTest {
        val local = FakeLocal()
        val repo = PreferencesRepository(local)

        repo.setTheme(Theme.Dark)
        repo.setNotificationsEnabled(false)

        // Reload: a fresh repository over the same underlying store.
        val reloaded = PreferencesRepository(local).observe().first()
        assertEquals(Theme.Dark, reloaded.theme)
        assertFalse(reloaded.notificationsEnabled)
    }
}
