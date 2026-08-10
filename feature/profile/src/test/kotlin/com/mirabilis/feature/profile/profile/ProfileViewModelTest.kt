package com.mirabilis.feature.profile.profile

import app.cash.turbine.test
import com.mirabilis.core.result.AppError
import com.mirabilis.core.result.Result
import com.mirabilis.domain.auth.model.User
import com.mirabilis.domain.auth.repository.ISessionRepository
import com.mirabilis.domain.auth.usecase.ObserveUserUseCase
import com.mirabilis.domain.profile.model.Theme
import com.mirabilis.domain.profile.model.UserPreferences
import com.mirabilis.domain.profile.repository.IPreferencesRepository
import com.mirabilis.domain.profile.repository.IProfileRepository
import com.mirabilis.domain.profile.usecase.ObservePreferencesUseCase
import com.mirabilis.domain.profile.usecase.SetNotificationsUseCase
import com.mirabilis.domain.profile.usecase.SetThemeUseCase
import com.mirabilis.domain.profile.usecase.UpdateDisplayNameUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/** US1 (FR-001): Profile loads the authenticated user from the session. */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val user = User(id = "u_1", phone = "+15551234567", displayName = "Ada Lovelace")

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

    private class FakeSession(user: User?) : ISessionRepository {
        val userFlow = MutableStateFlow(user)
        override fun observeAuthState(): Flow<Boolean> = MutableStateFlow(true)
        override fun observeUser(): Flow<User?> = userFlow
        override suspend fun signOut(): Result<Unit> = Result.Success(Unit)
        override suspend fun clearSession(): Result<Unit> = Result.Success(Unit)
    }

    /** Edit path double: on success writes back into the session flow so `observeUser` re-emits. */
    private class FakeProfile(
        private val session: FakeSession,
        private val fail: AppError? = null,
    ) : IProfileRepository {
        override suspend fun updateDisplayName(displayName: String): Result<User> {
            fail?.let { return Result.Error(it) }
            val updated = (session.userFlow.value ?: User("u_1", "+15551234567", null))
                .copy(displayName = displayName)
            session.userFlow.value = updated
            return Result.Success(updated)
        }
    }

    private class FakePrefs(initial: UserPreferences = UserPreferences()) : IPreferencesRepository {
        val state = MutableStateFlow(initial)
        override fun observe(): Flow<UserPreferences> = state
        override suspend fun setTheme(theme: Theme): Result<Unit> {
            state.value = state.value.copy(theme = theme); return Result.Success(Unit)
        }
        override suspend fun setNotificationsEnabled(enabled: Boolean): Result<Unit> {
            state.value = state.value.copy(notificationsEnabled = enabled); return Result.Success(Unit)
        }
    }

    private fun viewModel(
        session: FakeSession,
        profile: IProfileRepository = FakeProfile(session),
        prefs: IPreferencesRepository = FakePrefs(),
    ) = ProfileViewModel(
        ObserveUserUseCase(session),
        UpdateDisplayNameUseCase(profile),
        ObservePreferencesUseCase(prefs),
        SetThemeUseCase(prefs),
        SetNotificationsUseCase(prefs),
    )

    @Test
    fun `loads the authenticated user`() = runTest(dispatcher) {
        val vm = viewModel(FakeSession(user))

        vm.state.test {
            assertEquals(true, awaitItem().isLoading) // initial
            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(user, loaded.user)
            assertNull(loaded.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `null user surfaces a recoverable error`() = runTest(dispatcher) {
        val vm = viewModel(FakeSession(null))

        vm.state.test {
            assertEquals(true, awaitItem().isLoading) // initial
            val failed = awaitItem()
            assertFalse(failed.isLoading)
            assertNull(failed.user)
            assertEquals("Couldn't load your profile.", failed.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `editing a valid name persists and is reflected on the user`() = runTest(dispatcher) {
        val session = FakeSession(user)
        val vm = viewModel(session)
        advanceUntilIdle()
        assertEquals("Ada Lovelace", vm.state.value.nameDraft)

        vm.setIntent { ProfileIntent.NameChanged("Ada L.") }
        advanceUntilIdle()
        vm.setIntent { ProfileIntent.Save }
        advanceUntilIdle()

        val s = vm.state.value
        assertFalse(s.isSaving)
        assertNull(s.saveError)
        assertEquals("Ada L.", s.user?.displayName)
    }

    @Test
    fun `a save failure shows a recoverable error and does not change the user`() = runTest(dispatcher) {
        val session = FakeSession(user)
        val vm = viewModel(session, FakeProfile(session, AppError.Server(400, null)))
        advanceUntilIdle()

        vm.setIntent { ProfileIntent.NameChanged("X") }
        advanceUntilIdle()
        vm.setIntent { ProfileIntent.Save }
        advanceUntilIdle()

        val s = vm.state.value
        assertFalse(s.isSaving)
        assertEquals("That name isn't valid. Please try another.", s.saveError)
        assertEquals("Ada Lovelace", s.user?.displayName)
    }

    @Test
    fun `toggling preferences is persisted and reflected in state`() = runTest(dispatcher) {
        val prefs = FakePrefs()
        val vm = viewModel(FakeSession(user), prefs = prefs)
        advanceUntilIdle()
        assertEquals(Theme.System, vm.state.value.preferences.theme)

        vm.setIntent { ProfileIntent.SetTheme(Theme.Dark) }
        vm.setIntent { ProfileIntent.SetNotifications(false) }
        advanceUntilIdle()

        assertEquals(Theme.Dark, vm.state.value.preferences.theme)
        assertFalse(vm.state.value.preferences.notificationsEnabled)
        assertEquals(Theme.Dark, prefs.state.value.theme)
    }
}
