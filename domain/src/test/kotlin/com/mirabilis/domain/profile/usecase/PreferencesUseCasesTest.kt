package com.mirabilis.domain.profile.usecase

import app.cash.turbine.test
import com.mirabilis.domain.profile.FakePreferencesRepository
import com.mirabilis.domain.profile.model.Theme
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** US3/FR-004: observe reflects writes; set use cases delegate to the repository. */
class PreferencesUseCasesTest {

    @Test
    fun `observe reflects theme and notification writes`() = runTest {
        val repo = FakePreferencesRepository()
        val observe = ObservePreferencesUseCase(repo)
        val setTheme = SetThemeUseCase(repo)
        val setNotifications = SetNotificationsUseCase(repo)

        observe().test {
            val initial = awaitItem()
            assertEquals(Theme.System, initial.theme)
            assertTrue(initial.notificationsEnabled)

            setTheme(Theme.Dark)
            assertEquals(Theme.Dark, awaitItem().theme)

            setNotifications(false)
            assertEquals(false, awaitItem().notificationsEnabled)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
